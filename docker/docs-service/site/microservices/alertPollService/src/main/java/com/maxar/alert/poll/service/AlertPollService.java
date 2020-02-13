package com.maxar.alert.poll.service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.maxar.alert.model.Event;
import com.radiantblue.analytics.core.log.SourceLogger;

@Component
public class AlertPollService
{
	private static final AtomicBoolean paused = new AtomicBoolean(
			false);

	private static final Logger logger = SourceLogger.getLogger(AlertPollService.class.getName());

	@Autowired
	private ApiService apiService;

	/**
	 * Pause the polling service.
	 *
	 * @return True if the service was paused successfully, or false if the service
	 *         was already paused.
	 */
	public boolean pause() {
		final boolean isPaused = AlertPollService.paused.compareAndSet(	false,
																		true);

		if (isPaused) {
			logger.info("Paused polling");
		}
		else {
			logger.warn("Pause requested but polling was already paused");
		}

		return isPaused;
	}

	/**
	 * Resume the polling service.
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
	 * Worker method to fetch alerts from an external service and store them in the
	 * Alert service.
	 *
	 * This method is configured to run at a set interval set by the
	 * "alert-poll.scheduling.interval-milliseconds" configuration option. This
	 * method will only operate when the paused variable is set to false.
	 *
	 * @return True if the service polled for alerts, false otherwise.
	 */
	@Scheduled(fixedRateString = "${alert-poll.scheduling.interval-milliseconds}")
	public boolean pollForAlerts() {
		return pollForAlerts(false);
	}

	/**
	 * Worker method to fetch alerts from an external service and store them in the
	 * Alert service.
	 *
	 * This method is configured to run at a set interval set by the
	 * "alert-poll.scheduling.interval-milliseconds" configuration option. This
	 * method will only operate when the paused variable is set to false.
	 *
	 * @param forcePoll
	 *            If set to true, polling will occur regardless of the paused
	 *            field's setting.
	 * @return True if the service polled for alerts, false otherwise.
	 */
	public boolean pollForAlerts(
			final boolean forcePoll ) {
		if (!paused.get() || forcePoll) {
			logger.debug("Polling for alerts");

			final List<Event> events = apiService.getAllAlerts();

			if (events.isEmpty()) {
				logger.debug("Didn't find any events");
			}
			else {
				final List<String> existingIds = apiService.getAllStoredAlertIds();

				final List<Event> eventsToAdd = events.stream()
						.filter(Objects::nonNull)
						.filter(event -> event.getGeometryWkt() != null)
						.filter(event -> !existingIds.contains(event.getId()))
						.collect(Collectors.toList());

				logger.debug("Found " + events.size() + " events, adding " + eventsToAdd.size() + " events");

				eventsToAdd.forEach(apiService::postAlert);
			}

			return true;
		}
		else {
			return false;
		}
	}
}
