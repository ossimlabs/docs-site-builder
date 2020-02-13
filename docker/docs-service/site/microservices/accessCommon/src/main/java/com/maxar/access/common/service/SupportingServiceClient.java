package com.maxar.access.common.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.maxar.access.model.Access;
import com.maxar.asset.model.NameList;
import com.maxar.target.model.TargetModel;
import com.maxar.terrain.model.GoodTimeIntervalsRequest;
import com.maxar.terrain.model.GoodTimeIntervalsResponse;
import com.maxar.terrain.model.StateVector;
import com.maxar.terrain.model.TimeInterval;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.geodesy.EarthCenteredFrame;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

/**
 * Responsible for doing the work of consuming the access service's supporting
 * service calls and lookups.
 */
@Component
public class SupportingServiceClient
{

	private static Logger logger = SourceLogger.getLogger(SupportingServiceClient.class.getName());

	@Autowired
	private RestTemplate restTemplate;

	@Value("${microservices.access.samplingInterval_ms:1000}")
	private long samplingInterval_ms;

	@Value("${microservices.access.terrainUrl}")
	private String terrainUrl;

	@Value("${microservices.access.targetUrl}")
	private String targetUrl;

	@Value("${microservices.access.weatherUrl}")
	private String weatherUrl;

	@Value("${microservices.access.ephemerisUrl:}")
	private String ephemerisUrl;

	@Value("${microservices.access.missionUrl:}")
	private String missionUrl;

	@Value("${microservices.access.assetAllNamesUrl}")
	private String assetAllNamesUrl;

	public List<String> getAllAssetNames() {

		final ResponseEntity<NameList> response = restTemplate.getForEntity(assetAllNamesUrl,
																			NameList.class);
		if (response.getStatusCode()
				.isError()) {
			logger.error("Failed to retrieve all asset names, HTTP Status: " + response.getStatusCode());
			return null;
		}

		final List<String> returnNames = new ArrayList<>(
				Optional.ofNullable(response.getBody())
						.map(NameList::getNames)
						.orElse(Collections.emptyList()));

		return returnNames;
	}

	public TargetModel lookupTarget(
			final String targetID ) {

		final Map<String, String> params = new HashMap<>();
		params.put(	"id",
					targetID);

		final ResponseEntity<TargetModel> response = restTemplate.getForEntity(	targetUrl,
																				TargetModel.class,
																				params);
		if (response.getStatusCode()
				.isError()) {
			logger.error("Failed to retrieve target by ID, HTTP Status: " + response.getStatusCode());
			return null;
		}

		return response.getBody();
	}

	Double getWeatherDuringAccess(
			final Geometry geometry,
			final String atTimeISO8601 ) {

		final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(weatherUrl)
				.queryParam("geometry",
							new WKTWriter().write(geometry))
				.queryParam("date",
							atTimeISO8601);

		final ResponseEntity<Double> response = restTemplate.getForEntity(	builder.build()
				.encode()
				.toUri(),
																			Double.class);

		if (response.getStatusCode()
				.isError()) {
			logger.error("Failed to retrieve weather data, HTTP Status: " + response.getStatusCode());
			return null;
		}

		return response.getBody();
	}

	List<TimeInterval> getUnmaskedIntervalsFromTerrainMS(
			final Access access,
			final Geometry geometry,
			final Asset asset ) {

		final WKTWriter wktWriter = new WKTWriter();

		final GoodTimeIntervalsRequest request = new GoodTimeIntervalsRequest();
		request.setGeometryWkt(wktWriter.write(geometry));
		final List<StateVector> svs = new ArrayList<>();
		request.setStateVectors(svs);

		DateTime atTime = new DateTime(
				access.getStartTimeISO8601());
		final DateTime stop = new DateTime(
				access.getEndTimeISO8601());
		while (atTime.isBefore(stop)) {
			StateVector sv = new StateVector();
			sv.setAtTime(atTime);
			StateVectorsInFrame svif = asset.getStateVectors(	atTime,
																EarthCenteredFrame.ECEF);
			sv.setLatitude(svif.geodeticPosition()
					.latitude()
					.degrees());
			sv.setLongitude(svif.geodeticPosition()
					.longitude()
					.degrees());
			sv.setAltitude(svif.geodeticPosition()
					.altitude()
					.meters());
			svs.add(sv);
			atTime = atTime.plus(samplingInterval_ms);
			// Add the last moment of the access as the final sample
			if (!atTime.isBefore(stop)) {
				atTime = stop;
				sv = new StateVector();
				sv.setAtTime(atTime);
				svif = asset.getStateVectors(	atTime,
												EarthCenteredFrame.ECEF);
				svif.geodeticPosition()
						.latitude()
						.degrees();
				sv.setLatitude(svif.geodeticPosition()
						.latitude()
						.degrees());
				sv.setLongitude(svif.geodeticPosition()
						.longitude()
						.degrees());
				sv.setAltitude(svif.geodeticPosition()
						.altitude()
						.meters());
				svs.add(sv);
			}
		}

		RequestEntity<GoodTimeIntervalsRequest> requestEntity;
		try {
			requestEntity = RequestEntity.post(new URI(
					terrainUrl))
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.body(request);
			final ResponseEntity<GoodTimeIntervalsResponse> response = restTemplate.postForEntity(	terrainUrl,
																									requestEntity,
																									GoodTimeIntervalsResponse.class);

			if (response.getStatusCode()
					.isError() || (response.getBody() == null)
					|| (response.getBody()
							.getTimeIntervals() == null)) {
				logger.error("Failed to retrieve terrain data, HTTP Status: " + response.getStatusCode());
				return null;
			}
			return response.getBody()
					.getTimeIntervals();
		}
		catch (final URISyntaxException e) {
			logger.error("URI syntax exception parsing: " + terrainUrl);
			return null;
		}
	}
}
