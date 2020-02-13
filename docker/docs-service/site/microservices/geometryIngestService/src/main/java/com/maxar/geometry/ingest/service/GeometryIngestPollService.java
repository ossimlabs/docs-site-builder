package com.maxar.geometry.ingest.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.maxar.geometric.intersection.model.AreaOfInterest;
import com.maxar.geometry.ingest.translate.KmlAoiTranslator;
import com.radiantblue.analytics.core.log.SourceLogger;

@Component
public class GeometryIngestPollService
{
	private static final AtomicBoolean paused = new AtomicBoolean(
			false);

	private static final Logger logger = SourceLogger.getLogger(GeometryIngestPollService.class.getName());

	@Autowired
	private ApiService apiService;

	@Autowired
	private KmlAoiTranslator kmlAoiTranslator;

	/**
	 * Pause the polling for geometries.
	 *
	 * @return True if the service was paused successfully, or false if the service
	 *         was already paused.
	 */
	public boolean pause() {
		final boolean paused = GeometryIngestPollService.paused.compareAndSet(	false,
																				true);

		if (paused) {
			logger.info("Paused polling");
		}
		else {
			logger.warn("Pause requested but polling was already paused");
		}

		return paused;
	}

	/**
	 * Resume the polling for geometries.
	 *
	 * @return True if the service was resumed successfully, or false if the service
	 *         was not paused.
	 */
	public boolean resume() {
		final boolean resumed = paused.compareAndSet(	true,
														false);

		if (resumed) {
			logger.info("Resumed polling");
		}
		else {
			logger.warn("Resume requested but polling was not paused");
		}

		return resumed;
	}

	/**
	 * Worker method to fetch geometries from an external service and store them in
	 * the Geometric Intersection service.
	 *
	 * This method is configured to run at a set interval set by the
	 * "geometry-ingest.scheduling.interval-milliseconds" configuration option. This
	 * method will only operate when the paused variable is set to false.
	 *
	 * @return True if the service polled for geometries, false otherwise.
	 */
	@Scheduled(fixedRateString = "${geometry-ingest.scheduling.interval-milliseconds}")
	public boolean pollForGeometries() {
		return pollForGeometries(false);
	}

	/**
	 * Worker method to fetch geometries from an external service and store them in
	 * the Geometric Intersection service.
	 *
	 * This method is configured to run at a set interval set by the
	 * "geometry-ingest.scheduling.interval-milliseconds" configuration option. This
	 * method will only operate when the paused variable is set to false, or when
	 * the forcePoll parameter is set to true.
	 *
	 * @param forcePoll
	 *            If set to true, polling will occur regardless of the paused
	 *            field's setting.
	 * @return True if the service polled for geometries, false otherwise.
	 */
	public boolean pollForGeometries(
			final boolean forcePoll ) {
		if (!paused.get() || forcePoll) {
			logger.debug("Polling for geometries");

			final List<AreaOfInterest> aois = Optional.ofNullable(apiService.getRawAois())
					.map(kmlAoiTranslator::translateKmlToAois)
					.orElse(Collections.emptyList());

			if (aois.isEmpty()) {
				logger.debug("Didn't find any geometries");
			}
			else {
				final List<String> existingIds = apiService.getAllStoredAoiIds();

				final List<AreaOfInterest> aoisToAdd = aois.stream()
						.filter(Objects::nonNull)
						.filter(aoi -> aoi.getGeometryWkt() != null)
						.filter(aoi -> !existingIds.contains(aoi.getId()))
						.collect(Collectors.toList());

				logger.debug("Found " + aois.size() + " geometries, adding " + aoisToAdd.size() + " geometries");

				aoisToAdd.forEach(apiService::createAoi);
			}

			return true;
		}
		else {
			return false;
		}
	}
}
