package com.maxar.asset.common.service;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.maxar.asset.common.aircraft.AircraftTrackStateVectorProvider;
import com.maxar.asset.common.exception.MissionIdDoesNotExistException;
import com.maxar.asset.common.exception.NoEphemerisFoundException;
import com.maxar.asset.common.exception.NoMissionsFoundException;
import com.maxar.asset.common.exception.PropagatorTypeDoesNotExistException;
import com.maxar.ephemeris.model.EphemerisModel;
import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.ephemeris.model.StateVectorSetModel;
import com.maxar.ephemeris.model.TLEModel;
import com.maxar.ephemeris.model.VCMModel;
import com.maxar.mission.model.MissionModel;
import com.radiantblue.afspc.astrostds.mechanics.orbit.SPPropagator;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.mechanics.IStateVectorProvider;
import com.radiantblue.analytics.mechanics.orbit.J2Propagator;
import com.radiantblue.analytics.mechanics.orbit.ThreadSafeSGP4Propagator;
import com.radiantblue.analytics.mechanics.orbit.elementproviders.TLEElementProvider;
import com.radiantblue.analytics.mechanics.statevectors.InterpolatedTableStateVectorProvider;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

@Component
public class ApiService
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	@Value("${microservices.asset.mission.getMissionsByAssetAndTime}")
	private String getMissionsByAssetAndTimeUrl;

	@Value("${microservices.asset.ephemeris.getEphemerisByAssetAndTime}")
	private String getEphemerisByAssetAndTimeUrl;

	@Value("${microservices.asset.space.propagatorTypeThresholdDays}")
	private long PROPAGATOR_TYPE_THRESHOLD_DAYS;

	@Autowired
	private RestTemplate restTemplate;

	// Cache the SPPropagators due to the AFAS library not allowing multiple
	// copies of the same VCM to be initialized
	@Autowired
	private Cache<VCMModel, SPPropagator> spPropagatorCache;

	/**
	 * Use the Mission Track service to get the missions for an asset at the
	 * specified time.
	 *
	 * @param assetName
	 *            The name of the asset.
	 * @param atTime
	 *            The time to be used for the start and stop time query.
	 * @return A list of all missions for the asset at the specified time.
	 */
	public List<MissionModel> getAssetMissionsAtTime(
			final String assetName,
			final DateTime atTime ) {
		final URI uri = UriComponentsBuilder.fromHttpUrl(getMissionsByAssetAndTimeUrl)
				.queryParam("assetId",
							assetName)
				.queryParam("start",
							atTime.toString())
				.queryParam("stop",
							atTime.toString())
				.build()
				.encode()
				.toUri();

		final ParameterizedTypeReference<List<MissionModel>> typeReference = new ParameterizedTypeReference<>() {};
		final ResponseEntity<List<MissionModel>> response = restTemplate.exchange(	uri,
																					HttpMethod.GET,
																					null,
																					typeReference);

		return Optional.ofNullable(response.getBody())
				.orElse(Collections.emptyList());
	}

	/**
	 * Use the Mission Track service to get the missions for an asset between the
	 * specified times.
	 *
	 * @param assetName
	 *            The name of the asset.
	 * @param startTime
	 *            The time to be used for the start time of the query.
	 * @param stopTime
	 *            The time to be used for the stop time of the query.
	 * @return A list of all missions for the asset at the specified time range.
	 */
	public List<MissionModel> getAssetMissionsAtTime(
			final String assetName,
			final DateTime startTime,
			final DateTime stopTime ) {
		final URI uri = UriComponentsBuilder.fromHttpUrl(getMissionsByAssetAndTimeUrl)
				.queryParam("assetId",
							assetName)
				.queryParam("start",
							startTime.toString())
				.queryParam("stop",
							stopTime.toString())
				.build()
				.encode()
				.toUri();

		final ParameterizedTypeReference<List<MissionModel>> typeReference = new ParameterizedTypeReference<>() {};
		final ResponseEntity<List<MissionModel>> response = restTemplate.exchange(	uri,
																					HttpMethod.GET,
																					null,
																					typeReference);

		return Optional.ofNullable(response.getBody())
				.orElse(Collections.emptyList());
	}

	/**
	 * Use the Ephemeris service to get the latest ephemeris data for an asset.
	 *
	 * @param assetId
	 *            The ID of the aset (SCN).
	 * @param atTime
	 *            The time to get the ephemeris data for.
	 * @return The ephemeris for the specified asset at the specified time, or null
	 *         if it does not exist.
	 */
	public EphemerisModel getAssetEphemerisAtTime(
			final String assetId,
			final DateTime atTime ) {
		final URI uri = UriComponentsBuilder.fromHttpUrl(getEphemerisByAssetAndTimeUrl)
				.queryParam("scn",
							assetId)
				.queryParam("date",
							atTime.toString())
				.build()
				.encode()
				.toUri();

		final ResponseEntity<JsonNode> response = restTemplate.getForEntity(uri,
																			JsonNode.class);

		// No ephemeris for asset at atTime
		if (response.getBody() == null) {
			return null;
		}

		final ObjectMapper mapper = new ObjectMapper();

		try {
			switch (EphemerisType.valueOf(response.getBody()
					.get("type")
					.asText())) {
				case STATE_VECTOR_SET:
					return mapper.readValue(mapper.writeValueAsString(response.getBody()),
											StateVectorSetModel.class);
				case TLE:
					return mapper.readValue(mapper.writeValueAsString(response.getBody()),
											TLEModel.class);
				case VCM:
					return mapper.readValue(mapper.writeValueAsString(response.getBody()),
											VCMModel.class);
				default:
					return null;
			}
		}
		catch (final IOException e) {
			logger.warn("Error mapping ephemeris type: " + e.getMessage());
			return null;
		}
	}

	public String updateAssetEphemeris(
			final Asset asset,
			final DateTime atTime,
			@Nullable
			final String propagatorType )
			throws PropagatorTypeDoesNotExistException,
			NoEphemerisFoundException {

		String retPropagatorType = propagatorType;

		final EphemerisModel ephemeris = getAssetEphemerisAtTime(	Integer.toString(asset.getId()),
																	atTime);
		if (ephemeris == null) {
			throw new NoEphemerisFoundException(
					asset.getName(),
					atTime.toString());
		}

		if (ephemeris instanceof TLEModel) {
			final TLEModel tle = (TLEModel) ephemeris;
			if ((propagatorType == null) || propagatorType.isEmpty()) {
				final long tleAgeMillis = atTime.getMillis() - tle.getEpochMillis();
				final long thresholdMillis = PROPAGATOR_TYPE_THRESHOLD_DAYS * 24 * 60 * 60 * 1000;
				if (tleAgeMillis > thresholdMillis) {
					retPropagatorType = "J2";
				}
				else {
					retPropagatorType = "SGP4";
				}
				logger.debug("Requested time: " + atTime.toString() + " TLE epoch: " + new DateTime(
						tle.getEpochMillis()).toString() + " Selected propagator: " + retPropagatorType);
			}

			final TLEElementProvider tleProvider = new TLEElementProvider(
					tle.getDescription(),
					tle.getTleLineOne(),
					tle.getTleLineTwo());

			IStateVectorProvider svp;

			if (retPropagatorType.equals("J2")) {
				svp = new J2Propagator(
						tleProvider);
			}
			else if (retPropagatorType.equals("SGP4")) {
				svp = new ThreadSafeSGP4Propagator(
						tleProvider);
			}
			else {
				throw new PropagatorTypeDoesNotExistException(
						retPropagatorType);
			}

			asset.setPropagator(svp);
			asset.init();
		}
		else if (ephemeris instanceof StateVectorSetModel) {
			final StateVectorSetModel svs = (StateVectorSetModel) ephemeris;
			final StateVectorsInFrame[] svif = svs.getStateVectorsInFrame();
			final Duration step = svs.getStep();

			final InterpolatedTableStateVectorProvider itsvp = InterpolatedTableStateVectorProvider
					.createFromListOfStateVecs(	svif,
												step,
												true);

			asset.update(itsvp);
		}
		else if (ephemeris instanceof VCMModel) {
			// Clean up the cache to ensure that eviction handlers are called. This is
			// required due to how Guava cache evictions work.
			spPropagatorCache.cleanUp();

			final VCMModel vcm = (VCMModel) ephemeris;

			SPPropagator spPropagator = spPropagatorCache.getIfPresent(vcm);

			// Only create a new propagator if it does not already exist
			if (spPropagator == null) {
				spPropagator = new SPPropagator(
						vcm.getVcm());

				// Cache the new propagator
				spPropagatorCache.put(	vcm,
										spPropagator);
			}

			asset.update(spPropagator);
		}
		else {
			logger.warn("Could not update ephemeris for asset: " + asset.getName()
					+ "; ephemeris class not recognized: " + ephemeris.getClass()
							.getName());
		}

		return retPropagatorType;
	}

	public void updateAssetMission(
			final Asset asset,
			final DateTime startTime,
			final DateTime stopTime,
			@Nullable
			final String missionId )
			throws NoMissionsFoundException,
			MissionIdDoesNotExistException {

		final String assetId = String.valueOf(asset.getId());

		final List<MissionModel> missions = getAssetMissionsAtTime(	assetId,
																	startTime,
																	stopTime);
		if ((missions == null) || missions.isEmpty()) {
			throw new NoMissionsFoundException(
					assetId,
					startTime.toString());
		}

		// if no missionId was specified, then just use the first mission
		MissionModel mission = null;
		if ((missionId == null) || missionId.isEmpty()) {
			mission = missions.get(0);
		}
		// otherwise use the specified mission (if found)
		else {
			for (final MissionModel curMission : missions) {
				if (curMission.getId()
						.equals(missionId)) {
					mission = curMission;
					break;
				}
			}
		}
		if (mission == null) {
			throw new MissionIdDoesNotExistException(
					missionId,
					assetId,
					startTime.toString());
		}
		logger.debug("Using mission id " + mission.getId() + " for asset " + assetId);

		final IStateVectorProvider svp = new AircraftTrackStateVectorProvider(
				mission);

		asset.setPropagator(svp);
		asset.init();
	}

	public void updateAssetMission(
			final Asset asset,
			final DateTime atTime,
			@Nullable
			final String missionId )
			throws NoMissionsFoundException,
			MissionIdDoesNotExistException {
		updateAssetMission(	asset,
							atTime,
							atTime,
							missionId);
	}
}
