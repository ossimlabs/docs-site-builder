package com.maxar.cop.dm.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maxar.asset.model.IdList;
import com.maxar.asset.model.NameList;
import com.maxar.asset.model.OpBeam;
import com.maxar.cop.dm.cesium.CesiumTreeNode;
import com.maxar.mission.model.MissionModel;
import com.maxar.planning.model.image.CollectionWindowModel;
import com.maxar.planning.model.image.ImageFrameModel;
import com.maxar.planning.model.image.ImageOpModel;
import com.maxar.planning.model.tasking.TaskingModel;
import com.radiantblue.analytics.core.log.SourceLogger;

/**
 * Responsible for doing the work of consuming the COP service's supporting
 * service calls and lookups.
 */
@Component
public class CopServiceClient
{

	private static Logger logger = SourceLogger.getLogger(CopServiceClient.class.getName());

	@Autowired
	private RestTemplate restTemplate;

	@Value("${microservices.copdm.assetAllIdsUrl}")
	private String assetAllIdsUrl;

	@Value("${microservices.copdm.airborneAssetAllIdsUrl}")
	private String airborneAssetAllIdsUrl;

	@Value("${microservices.copdm.airborneAssetAllNamesUrl}")
	private String airborneAssetAllNamesUrl;

	@Value("${microservices.copdm.airborneMissionsUrl}")
	private String airborneMissionsUrl;

	@Value("${microservices.copdm.airborneTaskingCzmlUrl}")
	private String airborneTaskingCzmlUrl;

	@Value("${microservices.copdm.collectionWindowQueryUrl}")
	private String collectionWindowQueryUrl;

	@Value("${microservices.copdm.collectionWindowCzmlUrl}")
	private String collectionWindowCzmlUrl;

	@Value("${microservices.copdm.cesiumUiRequestUrl}")
	private String cesiumUiRequestUrl;

	@Value("${microservices.copdm.postCzmlPacketsUrl}")
	private String postCzmlPacketsUrl;

	@Value("${microservices.copdm.taskingDurationMillis}")
	private int taskingDurationMillis;

	@Value("${microservices.copdm.cesiumSessionId}")
	private String cesiumSessionId;

	private static final String SESSION_KEY = "session";

	private static final String STATUS_CODE_LABEL = "\n\t status code: ";

	private static final String ASSET_ID_LABEL = "assetId";

	private static final String MISSION_ID_LABEL = "missionId";

	private static final String SCN_LABEL = "scn";

	private static final String START_LABEL = "start";

	private static final String STOP_LABEL = "stop";

	public List<String> getSpaceAssetIDs() {

		final ResponseEntity<IdList> response = restTemplate.getForEntity(	assetAllIdsUrl,
																			IdList.class);
		if (response.getStatusCode()
				.isError()) {
			logger.error("Failed to retrieve all asset IDs, HTTP Status: " + response.getStatusCode());
			return Collections.emptyList();
		}

		final List<String> returnNames = new ArrayList<>();
		for (final String id : response.getBody()
				.getIds()) {
			returnNames.add(id);
		}

		return returnNames;
	}

	public void postCzmlToUI(
			final ObjectNode requestObject ) {
		postCzmlToUI(Collections.singletonList(requestObject));
	}

	public void postCzmlToUI(
			final List<ObjectNode> requestObjects ) {

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		for (final ObjectNode requestObject : requestObjects) {

			final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(cesiumUiRequestUrl)
					.queryParam(SESSION_KEY,
								cesiumSessionId);

			final HttpEntity<JsonNode> requestEntity = new HttpEntity<>(
					requestObject,
					headers);

			try {
				final ResponseEntity<Boolean> response = restTemplate.postForEntity(uriBuilder.toUriString(),
																					requestEntity,
																					Boolean.class);

				if (response.getStatusCode()
						.isError() || (response.getBody() == null) || !response.getBody()) {
					logger.error("Failed to post CZML retrieval URL to client: \n\t " + requestObject.get("url")
							+ STATUS_CODE_LABEL + response.getStatusCode()
									.toString());
				}
			}
			catch (final RestClientException e) {
				logger.error("Failed to post CZML retrieval URL to client: \n\t " + requestObject.get("url")
						+ STATUS_CODE_LABEL + e.getMessage());
			}
		}
	}

	public List<String> getAirborneAssetNames() {

		final ResponseEntity<NameList> response = restTemplate.getForEntity(airborneAssetAllNamesUrl,
																			NameList.class);
		if (response.getStatusCode()
				.isError()) {
			logger.error("Failed to retrieve all airborne asset names, HTTP Status: " + response.getStatusCode());
			return Collections.emptyList();
		}

		final List<String> returnNames = new ArrayList<>();
		for (final String id : response.getBody()
				.getNames()) {
			returnNames.add(id);
		}

		return returnNames;
	}

	public List<String> getAirborneAssetIDs() {

		final ResponseEntity<IdList> response = restTemplate.getForEntity(	airborneAssetAllIdsUrl,
																			IdList.class);
		if (response.getStatusCode()
				.isError()) {
			logger.error("Failed to retrieve all airborne asset IDs, HTTP Status: " + response.getStatusCode());
			return Collections.emptyList();
		}

		final List<String> returnIds = new ArrayList<>();
		for (final String id : response.getBody()
				.getIds()) {
			returnIds.add(id);
		}

		return returnIds;
	}

	public List<String> getAirborneMissionIDs(
			final String assetId,
			final DateTime start,
			final DateTime stop ) {

		final List<String> missionIds = new ArrayList<>();

		final URI uri = UriComponentsBuilder.fromHttpUrl(airborneMissionsUrl)
				.queryParam(ASSET_ID_LABEL,
							assetId)
				.queryParam(START_LABEL,
							start.toString())
				.queryParam(STOP_LABEL,
							stop.toString())
				.build()
				.encode()
				.toUri();

		final ResponseEntity<List<MissionModel>> response = restTemplate.exchange(	uri,
																					HttpMethod.GET,
																					null,
																					new ParameterizedTypeReference<List<MissionModel>>() {});

		final List<MissionModel> missions = response.getBody();
		if (missions != null) {
			for (final MissionModel mission : missions) {
				missionIds.add(mission.getId());
			}
		}

		return missionIds;
	}

	public List<OpBeam> getAirborneOps(
			final String missionId,
			final DateTime start,
			final DateTime stop ) {

		final URI uri = UriComponentsBuilder.fromHttpUrl(airborneTaskingCzmlUrl)
				.queryParam(MISSION_ID_LABEL,
							missionId)
				.queryParam(START_LABEL,
							start.toString())
				.queryParam(STOP_LABEL,
							stop.toString())
				.build()
				.encode()
				.toUri();

		final ResponseEntity<List<TaskingModel>> response = restTemplate.exchange(	uri,
																					HttpMethod.GET,
																					null,
																					new ParameterizedTypeReference<List<TaskingModel>>() {});

		final List<TaskingModel> taskings = response.getBody();
		final List<OpBeam> ops = new ArrayList<>();

		if (taskings != null) {
			for (final TaskingModel tasking : taskings) {
				final OpBeam op = new OpBeam();
				op.setStartTimeISO8601(new DateTime(
						tasking.getTotTimeMillis()).toString());
				op.setStopTimeISO8601(new DateTime(
						tasking.getTotTimeMillis() + taskingDurationMillis).toString());
				op.setBeamCentroidGeo(tasking.getTaskingCoord());
				ops.add(op);
			}
		}

		return ops;
	}

	public List<CollectionWindowModel> getCws(
			final String assetId,
			final DateTime start,
			final DateTime stop ) {

		final URI uri = UriComponentsBuilder.fromHttpUrl(collectionWindowQueryUrl)
				.queryParam(SCN_LABEL,
							assetId)
				.queryParam(START_LABEL,
							start.toString())
				.queryParam(STOP_LABEL,
							stop.toString())
				.build()
				.encode()
				.toUri();

		final ResponseEntity<List<CollectionWindowModel>> response = restTemplate.exchange(	uri,
																							HttpMethod.GET,
																							null,
																							new ParameterizedTypeReference<List<CollectionWindowModel>>() {});

		return response.getBody();
	}

	public List<OpBeam> getSpaceOps(
			final String assetId,
			final DateTime start,
			final DateTime stop ) {

		final URI uri = UriComponentsBuilder.fromHttpUrl(collectionWindowQueryUrl)
				.queryParam(SCN_LABEL,
							assetId)
				.queryParam(START_LABEL,
							start.toString())
				.queryParam(STOP_LABEL,
							stop.toString())
				.build()
				.encode()
				.toUri();

		final ResponseEntity<List<CollectionWindowModel>> response = restTemplate.exchange(	uri,
																							HttpMethod.GET,
																							null,
																							new ParameterizedTypeReference<List<CollectionWindowModel>>() {});

		final List<CollectionWindowModel> cws = response.getBody();
		final List<OpBeam> ops = new ArrayList<>();

		if (cws != null) {
			for (final CollectionWindowModel cw : cws) {
				for (final ImageOpModel imageOp : cw.getImageOps()) {
					for (final ImageFrameModel imageFrame : imageOp.getImageFrames()) {
						final OpBeam op = new OpBeam();
						op.setStartTimeISO8601(new DateTime(
								imageOp.getOpStartTimeMillis()).toString());
						op.setStopTimeISO8601(new DateTime(
								imageOp.getOpEndTimeMillis()).toString());
						op.setBeamCentroidGeo(imageFrame.getCenterCoord());
						ops.add(op);
					}
				}
			}
		}

		return ops;
	}

	public void createParentNode(
			final CesiumTreeNode node ) {

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(postCzmlPacketsUrl)
				.queryParam(SESSION_KEY,
							cesiumSessionId);

		final HttpEntity<List<JsonNode>> requestEntity = new HttpEntity<>(
				Collections.singletonList(node.toCzml()),
				headers);

		try {
			final ResponseEntity<Boolean> response = restTemplate.postForEntity(uriBuilder.toUriString(),
																				requestEntity,
																				Boolean.class);

			if (response.getStatusCode()
					.isError() || (response.getBody() == null) || !response.getBody()) {
				logger.error("Failed to post CZML request to client: \n\t " + node + STATUS_CODE_LABEL
						+ response.getStatusCode()
								.toString());
			}
		}
		catch (final RestClientException e) {
			logger.error("Failed to post CZML request to client: \n\t " + node + STATUS_CODE_LABEL + e.getMessage());
		}

	}

	public void createParentNode(
			final String nodeId ) {
		createParentNode(new CesiumTreeNode(
				nodeId,
				nodeId,
				null));
	}

	public void postPackets(
			final List<JsonNode> packets,
			final String parentId ) {

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(postCzmlPacketsUrl)
				.queryParam(SESSION_KEY,
							cesiumSessionId);
		if (parentId != null) {
			uriBuilder = uriBuilder.queryParam(	"parent",
												parentId);

		}

		final HttpEntity<List<JsonNode>> requestEntity = new HttpEntity<>(
				packets,
				headers);

		try {
			final ResponseEntity<Boolean> response = restTemplate.postForEntity(uriBuilder.toUriString(),
																				requestEntity,
																				Boolean.class);

			if (response.getStatusCode()
					.isError() || (response.getBody() == null) || !response.getBody()) {
				logger.error("Failed to post CZML packets to client, status code: " + response.getStatusCode()
						.toString());
			}
		}
		catch (final RestClientException e) {
			logger.error("Failed to post CZML packets to client, status code: " + e.getMessage());
		}

	}

	public List<JsonNode> retrieveCzml(
			final ObjectNode czmlRequest ) {

		HttpMethod httpMethod = HttpMethod.GET;
		HttpEntity<JsonNode> requestEntity = null;

		if (czmlRequest.get("body") != null) {
			httpMethod = HttpMethod.POST;
			requestEntity = new HttpEntity<>(
					czmlRequest.get("body"));
		}

		try {
			final ResponseEntity<List<JsonNode>> entity = restTemplate.exchange(new URI(
					czmlRequest.get("url")
							.asText()),
																				httpMethod,
																				requestEntity,
																				new ParameterizedTypeReference<List<JsonNode>>() {});

			if (entity.getBody() != null) {
				return entity.getBody();
			}
		}
		catch (final RestClientException e) {
			logger.error("Failed to retrieve CZML packets from service url " + czmlRequest.get("url")
					.asText() + " status code: " + e.getMessage());
		}
		catch (final URISyntaxException e) {
			logger.error("Invalid service url: " + czmlRequest.get("url")
					.asText());
		}

		return new ArrayList<>();
	}

}
