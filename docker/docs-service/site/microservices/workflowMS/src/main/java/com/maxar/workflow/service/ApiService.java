package com.maxar.workflow.service;

import static com.maxar.workflow.utils.UriUtils.putFormatCzml;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maxar.access.model.Access;
import com.maxar.access.model.AccessConstraint;
import com.maxar.access.model.AccessGenerationRequest;
import com.maxar.access.model.AccessValues;
import com.maxar.access.model.UntrimmedAccess;
import com.maxar.asset.model.AssetSmearWithBeamsRequest;
import com.maxar.asset.model.IdList;
import com.maxar.asset.model.NameList;
import com.maxar.asset.model.SpaceAssetModel;
import com.maxar.common.exception.BadRequestException;
import com.maxar.ephemeris.model.EphemerisModel;
import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.ephemeris.model.TLEModel;
import com.maxar.opgen.model.OpSpaceRequest;
import com.maxar.spaceobjectcatalog.model.SpaceObject;
import com.maxar.target.model.TargetModel;
import com.maxar.weather.model.weather.WeatherByDateAndGeometryRequest;
import com.maxar.weather.model.weather.WeatherByDateAndGeometryRequestBody;
import com.maxar.workflow.exception.CzmlGenerationException;
import com.maxar.workflow.model.AccessDetailsRequest;
import com.maxar.workflow.types.CesiumAssetSmearRequest;
import com.maxar.workflow.types.CesiumRequest;
import com.maxar.workflow.types.CesiumSpaceAccessRequest;
import com.maxar.workflow.types.CesiumSpaceOpsRequest;
import com.maxar.workflow.types.CesiumWeatherRequest;

/**
 * Handles the interaction between the Workflow service and other service
 * endpoints.
 */
@Component
class ApiService
{
	private static final String GEOMETRY_PARAMETER = "geometry";

	private static final String SESSION_PARAMETER = "session";

	private static final String ID_PARAMETER = "id";

	private static final String DATE_PARAMETER = "date";

	private static final String ACCESSES_PARENT_ID = "Accesses";

	private static final String PARENT_ID = "parent";

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	@Autowired
	private RestTemplate restTemplate;

	@Value("${microservices.workflow.access.getAccesses}")
	private String accessGetAccessesUrl;

	@Value("${microservices.workflow.access.getAccessConstraintNames}")
	private String accessGetAccessConstraintNamesUrl;

	@Value("${microservices.workflow.weather.getWeatherByDateAndGeometry}")
	private String weatherByDateAndGeometryUrl;

	@Value("${microservices.workflow.weather.getWeather}")
	private String weatherUrl;

	@Value("${microservices.workflow.asset.getSpaceSmears}")
	private String spaceSmearsUrl;

	@Value("${microservices.workflow.asset.fovAnimationIntervalSec}")
	private Integer fovAnimationIntervalSec;

	@Value("${microservices.workflow.opgen.getSpaceOps}")
	private String spaceOpsUrl;

	@Value("${microservices.workflow.opgen.spaceOpsSamplingIntervalMillis}")
	private Integer spaceOpsSamplingIntervalMillis;

	@Value("${microservices.workflow.access.getAccessDetails}")
	private String accessGetAccessDetailsUrl;

	@Value("${microservices.workflow.cesium.deleteCzml}")
	private String cesiumDeleteCzmlUrl;

	@Value("${microservices.workflow.cesium.displayCzml}")
	private String cesiumDisplayCzmlUrl;

	@Value("${microservices.workflow.target.getByGeometry}")
	private String targetGetByGeometryUrl;

	@Value("${microservices.workflow.target.getById}")
	private String targetGetByIdUrl;

	@Value("${microservices.workflow.asset.getSpaceAssets}")
	private String assetGetSpaceAssets;

	@Value("${microservices.workflow.asset.getSpaceAssetsIDs}")
	private String assetGetSpaceAssetsIDs;

	@Value("${microservices.workflow.asset.getSpaceAssetModelWithID}")
	private String assetGetSpaceAssetModelWithID;

	@Value("${microservices.workflow.asset.getSpaceAssetIdByName}")
	private String assetGetSpaceAssetIdByName;

	@Value("${microservices.workflow.spaceobjectcatalog.getAssetEphermeridesByScn}")
	private String catalogGetAssetEphermeridesByScn;

	@Value("${microservices.workflow.opgen.displayOpsInTree}")
	private boolean displayOpsInTree;

	@Value("${microservices.workflow.asset.displaySmearInTree}")
	private boolean displaySmearInTree;

	@Value("${microservices.workflow.weather.displayWeatherInTree}")
	private boolean displayWeatherInTree;

	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Get a list of targets by a geometry WKT.
	 *
	 * @param geometryWkt
	 *            The well-known text representation for the geometry.
	 * @return A list of targets if any match the input geometry.
	 */
	List<TargetModel> getTargetsByGeometry(
			final String geometryWkt ) {
		final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(targetGetByGeometryUrl)
				.queryParam(GEOMETRY_PARAMETER,
							geometryWkt)
				.queryParam("genEstimatedTarget",
							true);

		final ParameterizedTypeReference<List<TargetModel>> typeReference = new ParameterizedTypeReference<>() {};

		final ResponseEntity<List<TargetModel>> response = restTemplate.exchange(	builder.build()
				.encode()
				.toUri(),
																					HttpMethod.GET,
																					null,
																					typeReference);

		return Optional.ofNullable(response.getBody())
				.orElse(Collections.emptyList());
	}

	/**
	 * Get a target by its ID.
	 *
	 * @param targetId
	 *            The ID of the target to get.
	 * @return The Target with the ID if it exists.
	 */
	Optional<TargetModel> getTargetById(
			final String targetId ) {
		final Map<String, String> uriVariables = new HashMap<>();
		uriVariables.put(	ID_PARAMETER,
							targetId);

		final ResponseEntity<TargetModel> response = restTemplate.getForEntity(	targetGetByIdUrl,
																				TargetModel.class,
																				uriVariables);

		return Optional.ofNullable(response.getBody());
	}

	/**
	 * Get accesses for a geometry in a time window.
	 *
	 * @param geometryWkt
	 *            The well-known text representation for the geometry.
	 * @param startTime
	 *            The start time of the search window (ISO-8601).
	 * @param endTime
	 *            The end time of the search window (ISO-8601).
	 * @param accessConstraints
	 *            The list of constraints to query against
	 * @param assetStartTimeBufferMs
	 *            The time in milliseconds to pad the start of the access for asset
	 *            path generation in CZML
	 * @param assetEndTimeBufferMs
	 *            The time in milliseconds to pad the end of the access for asset
	 *            path generation in CZML
	 * @return A list of accesses if there are any available.
	 */
	List<UntrimmedAccess> getSpaceAccessesByGeometryAndTimes(
			final String geometryWkt,
			final String startTime,
			final String endTime,
			final List<String> spaceAssetIds,
			final List<AccessConstraint> accessConstraints,
			final Long assetStartTimeBufferMs,
			final Long assetEndTimeBufferMs ) {

		final AccessGenerationRequest request = new AccessGenerationRequest();
		request.setTgtOrGeometryWkt(geometryWkt);
		request.setStartTimeISO8601(startTime);
		request.setEndTimeISO8601(endTime);
		request.setAccessConstraints(Objects.requireNonNullElse(accessConstraints,
																Collections.emptyList()));
		request.setAssetIDs(spaceAssetIds);

		final ParameterizedTypeReference<List<UntrimmedAccess>> typeReference = new ParameterizedTypeReference<>() {};

		final ResponseEntity<List<UntrimmedAccess>> response = restTemplate.exchange(	accessGetAccessesUrl,
																						HttpMethod.POST,
																						new HttpEntity<>(
																								request),
																						typeReference);

		final Optional<List<UntrimmedAccess>> optionalAccesses = Optional.ofNullable(response.getBody());

		optionalAccesses.ifPresent(accesses -> {

			for (final UntrimmedAccess untrimmed : accesses) {
				final String accessId = untrimmed.getAccessId();
				final AccessGenerationRequest accRequest = new AccessGenerationRequest();
				accRequest.setTgtOrGeometryWkt(geometryWkt);
				accRequest.setStartTimeISO8601(untrimmed.getStartTimeISO8601());
				accRequest.setEndTimeISO8601(untrimmed.getEndTimeISO8601());
				accRequest.setAccessConstraints(Collections.emptyList());
				accRequest.setAssetIDs(Collections.singletonList(untrimmed.getAssetName()));
				accRequest.setAssetStartTimeBufferMs(assetStartTimeBufferMs);
				accRequest.setAssetEndTimeBufferMs(assetEndTimeBufferMs);
				final List<JsonNode> czml = getCzml(accRequest);
				final List<JsonNode> commonCzml = new ArrayList<>(
						czml);

				final DateTime startDateTime;
				final DateTime stopDateTime;

				try {
					startDateTime = ISODateTimeFormat.dateTimeParser()
							.parseDateTime(untrimmed.getStartTimeISO8601());

					stopDateTime = ISODateTimeFormat.dateTimeParser()
							.parseDateTime(untrimmed.getEndTimeISO8601());
				}
				catch (final RuntimeException e) {
					throw new BadRequestException(
							e.getMessage());
				}

				// Weather
				czml.addAll(setParentAndVisibility(	accessId,
													displayWeatherInTree,
													generateCzmlForAccessWeather(	generateWeatherByDateAndGeometryRequest(untrimmed
															.getStartTimeISO8601(),
																															geometryWkt),
																					accessId)));

				// Space Asset Smear and FOV
				czml.addAll(setParentAndVisibility(	accessId,
													displaySmearInTree,
													generateCzmlForAccessSmearAndFOV(	generateAssetSmearWithBeamsRequest(startDateTime,
																														stopDateTime,
																														untrimmed
																																.getAssetID()),
																						accessId)));

				// OpGen
				final OpSpaceRequest opRequest = new OpSpaceRequest();

				opRequest.setAssetName(untrimmed.getAssetName());
				opRequest.setSensorModeName(untrimmed.getSensorMode());
				opRequest.setTargetGeometryWkt(geometryWkt);
				opRequest.setStartTime(startDateTime);
				opRequest.setEndTime(stopDateTime);
				opRequest.setOpSampleTime_ms(spaceOpsSamplingIntervalMillis);
				opRequest.setPropagatorType(untrimmed.getPropagatorType());
				czml.addAll(setParentAndVisibility(	accessId,
													displayOpsInTree,
													generateCzmlSpaceOps(	opRequest,
																			accessId)));

				untrimmed.setCzml(czml);
				if (untrimmed.getTrimmedAccesses() != null) {
					for (final Access trimmed : untrimmed.getTrimmedAccesses()) {
						trimmed.setCzml(generateCzmlForTrimmedAccess(	commonCzml,
																		trimmed,
																		untrimmed,
																		geometryWkt,
																		accessId));
					}
				}
			}
		});

		return new ArrayList<>(
				optionalAccesses.orElse(Collections.emptyList()));
	}

	/**
	 * Generate czml for trimmed access
	 **/
	List<JsonNode> generateCzmlForTrimmedAccess(
			final List<JsonNode> commonCzml,
			final Access trimmed,
			final UntrimmedAccess untrimmed,
			final String geometryWkt,
			final String accessId ) {

		final DateTime trimmedStartDateTime;
		final DateTime trimmedStopDateTime;
		final List<JsonNode> trimmedCzml = new ArrayList<>(
				commonCzml);
		try {
			trimmedStartDateTime = ISODateTimeFormat.dateTimeParser()
					.parseDateTime(trimmed.getStartTimeISO8601());

			trimmedStopDateTime = ISODateTimeFormat.dateTimeParser()
					.parseDateTime(trimmed.getEndTimeISO8601());
		}
		catch (final RuntimeException e) {
			throw new BadRequestException(
					e.getMessage());
		}

		// Weather
		trimmedCzml.addAll(setParentAndVisibility(	accessId,
													displayWeatherInTree,
													generateCzmlForAccessWeather(	generateWeatherByDateAndGeometryRequest(trimmed
															.getStartTimeISO8601(),
																															geometryWkt),
																					accessId)));

		// Space Asset Smear and FOV
		trimmedCzml.addAll(setParentAndVisibility(	accessId,
													displaySmearInTree,
													generateCzmlForAccessSmearAndFOV(	generateAssetSmearWithBeamsRequest(trimmedStartDateTime,
																														trimmedStopDateTime,
																														untrimmed
																																.getAssetID()),
																						accessId)));

		// OpGen
		final OpSpaceRequest trimmedOpRequest = new OpSpaceRequest();

		trimmedOpRequest.setAssetName(untrimmed.getAssetName());
		trimmedOpRequest.setSensorModeName(untrimmed.getSensorMode());
		trimmedOpRequest.setTargetGeometryWkt(geometryWkt);
		trimmedOpRequest.setStartTime(trimmedStartDateTime);
		trimmedOpRequest.setEndTime(trimmedStopDateTime);
		trimmedOpRequest.setOpSampleTime_ms(spaceOpsSamplingIntervalMillis);
		trimmedOpRequest.setPropagatorType(untrimmed.getPropagatorType());
		trimmedCzml.addAll(setParentAndVisibility(	accessId,
													displayOpsInTree,
													generateCzmlSpaceOps(	trimmedOpRequest,
																			accessId)));
		return trimmedCzml;
	}

	WeatherByDateAndGeometryRequest generateWeatherByDateAndGeometryRequest(
			final String atTime,
			final String geometryWkt ) {
		final WeatherByDateAndGeometryRequest weatherRequest = new WeatherByDateAndGeometryRequest();

		weatherRequest.setDateTimeISO8601(atTime);
		weatherRequest.setGeometryWKT(geometryWkt);

		return weatherRequest;
	}

	private AssetSmearWithBeamsRequest generateAssetSmearWithBeamsRequest(
			final DateTime startDateTime,
			final DateTime stopDateTime,
			final String assetId ) {
		final AssetSmearWithBeamsRequest assetSmearReq = new AssetSmearWithBeamsRequest();

		assetSmearReq.setStartTimeISO8601(startDateTime.toString());
		assetSmearReq.setStopTimeISO8601(stopDateTime.toString());
		assetSmearReq.setAssetId(assetId);
		assetSmearReq.setForFrameIncrementSec(fovAnimationIntervalSec);

		return assetSmearReq;
	}

	/**
	 * Get access details from the space Access service for an access time.
	 *
	 * @param assetName
	 *            The name of the asset.
	 * @param atTime
	 *            The exact time to get the access details for.
	 * @param propagatorType
	 *            The propagator type used to generate the access.
	 * @param sensorMode
	 *            The sensor mode of the access.
	 * @param geometryWkt
	 *            The geometry of the access target.
	 * @return The access details for the access at the specified time.
	 */
	AccessValues getSpaceAccessDetails(
			final String assetName,
			final DateTime atTime,
			final String propagatorType,
			final String sensorMode,
			final String geometryWkt ) {
		final URI uri = putAccessParameters(UriComponentsBuilder.fromHttpUrl(accessGetAccessDetailsUrl),
											assetName,
											atTime,
											sensorMode,
											geometryWkt).queryParam("propagatorType",
																	propagatorType)
													.build()
													.encode()
													.toUri();

		final ResponseEntity<AccessValues> response = restTemplate.getForEntity(uri,
																				AccessValues.class);

		return response.getBody();
	}

	private List<JsonNode> generateCzmlForAccessWeather(
			final WeatherByDateAndGeometryRequest request,
			final String parentAccessId ) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(weatherByDateAndGeometryUrl);
		builder = putFormatCzml(builder);
		final String url = builder.build()
				.encode()
				.toUri()
				.toString();

		List<JsonNode> weatherNodes;

		try {
			final WeatherByDateAndGeometryRequestBody wbody = new WeatherByDateAndGeometryRequestBody();

			final List<WeatherByDateAndGeometryRequest> wlist = new ArrayList<>();

			wlist.add(request);

			wbody.setWeatherRequestList(wlist);

			final CesiumWeatherRequest accessRequest = new CesiumWeatherRequest(
					parentAccessId,
					false,
					false,
					url,
					wbody);
			weatherNodes = requestCzml(accessRequest);
		}
		catch (final Exception e) {
			return Collections.emptyList();
		}
		return weatherNodes;
	}

	Double getAccessWeather(
			final WeatherByDateAndGeometryRequest request ) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(weatherUrl);
		builder.queryParam(	DATE_PARAMETER,
							request.getDateTimeISO8601())
				.queryParam(GEOMETRY_PARAMETER,
							request.getGeometryWKT());
		final ResponseEntity<Double> response = restTemplate.getForEntity(	builder.build()
				.encode()
				.toUri(),
																			Double.class);

		return response.getBody();
	}

	private List<JsonNode> generateCzmlForAccessSmearAndFOV(
			final AssetSmearWithBeamsRequest request,
			final String parentAccessId ) {
		final String url = putFormatCzml(UriComponentsBuilder.fromHttpUrl(spaceSmearsUrl)).build()
				.encode()
				.toUri()
				.toString();

		List<JsonNode> assetSmearNodes;

		try {
			final CesiumAssetSmearRequest accessRequest = new CesiumAssetSmearRequest(
					parentAccessId,
					false,
					false,
					url,
					request);

			assetSmearNodes = requestCzml(accessRequest);
		}
		catch (final Exception e) {
			return Collections.emptyList();
		}
		return assetSmearNodes;
	}

	private List<JsonNode> setParentAndVisibility(
			final String parentId,
			final boolean displayInTree,
			final List<JsonNode> czmlPackets ) {
		if (parentId != null) {
			czmlPackets.stream()
					.filter(node -> !node.hasNonNull(PARENT_ID))
					.forEach(node -> ((ObjectNode) node).put(	PARENT_ID,
																parentId));

			if (!displayInTree) {
				czmlPackets.forEach(node -> {
					final ObjectNode layerNode = mapper.createObjectNode();
					layerNode.put(	"showAsLayer",
									false);
					((ObjectNode) node).set("layerControl",
											layerNode);
				});
			}
		}

		return czmlPackets;
	}

	private List<JsonNode> generateCzmlSpaceOps(
			final OpSpaceRequest request,
			final String parentAccessId ) {

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(spaceOpsUrl);
		builder = putFormatCzml(builder);
		final String url = builder.build()
				.encode()
				.toUri()
				.toString();

		List<JsonNode> spaceOpsNodes;

		try {
			final CesiumSpaceOpsRequest accessRequest = new CesiumSpaceOpsRequest(
					parentAccessId,
					false,
					false,
					url,
					request);

			spaceOpsNodes = requestCzml(accessRequest);
		}
		catch (final Exception e) {
			return Collections.emptyList();
		}
		return spaceOpsNodes;
	}

	/**
	 * Retrieves the list of CZML packets for the supplied access request
	 *
	 * @param request
	 *            Space access request
	 * @return List of CZML packets
	 */
	private List<JsonNode> getCzml(
			final AccessGenerationRequest request ) {

		final UriComponentsBuilder builder = putFormatCzml(UriComponentsBuilder.fromHttpUrl(accessGetAccessesUrl));

		final CesiumRequest accessRequest = new CesiumSpaceAccessRequest(
				ACCESSES_PARENT_ID,
				true,
				true,
				builder.build()
						.encode()
						.toUri()
						.toString(),
				request);

		final Optional<List<JsonNode>> czmlResponse = Optional.ofNullable(restTemplate.exchange(accessRequest.getUrl(),
																								HttpMethod.POST,
																								new HttpEntity<>(
																										accessRequest
																												.getBody()),
																								new ParameterizedTypeReference<List<JsonNode>>() {})
				.getBody());

		return czmlResponse.orElse(new ArrayList<>());
	}

	private List<JsonNode> requestCzml(
			final CesiumRequest cesiumRequest ) {
		final Optional<List<JsonNode>> czmlResponse = Optional.ofNullable(restTemplate.exchange(cesiumRequest.getUrl(),
																								HttpMethod.POST,
																								new HttpEntity<>(
																										cesiumRequest
																												.getBody()),
																								new ParameterizedTypeReference<List<JsonNode>>() {})
				.getBody());

		return czmlResponse.orElse(Collections.emptyList());
	}

	/**
	 * Send a Cesium request to delete all czml data
	 *
	 * @throws CzmlGenerationException
	 *             Thrown if there was an error making the request
	 * @param session
	 *            session to clear
	 **/

	void deleteCzml(
			final String session )
			throws CzmlGenerationException {
		try {

			final UriComponentsBuilder builderRequest = UriComponentsBuilder.fromHttpUrl(cesiumDeleteCzmlUrl)
					.queryParam(SESSION_PARAMETER,
								session);

			final String url = builderRequest.build()
					.encode()
					.toUri()
					.toString();

			restTemplate.delete(url);

		}
		catch (final Exception e) {
			throw new CzmlGenerationException(
					e.getMessage());

		}
	}

	void displayCzml(
			final String session,
			final List<JsonNode> czml )
			throws CzmlGenerationException {
		try {
			final UriComponentsBuilder builderRequest = UriComponentsBuilder.fromHttpUrl(cesiumDisplayCzmlUrl)
					.queryParam("generateParent",
								true)
					.queryParam(PARENT_ID,
								ACCESSES_PARENT_ID)
					.queryParam(SESSION_PARAMETER,
								session);

			final String url = builderRequest.build()
					.encode()
					.toUri()
					.toString();

			final ResponseEntity<String> response = restTemplate.postForEntity(	url,
																				czml,
																				String.class);

			if (response.getStatusCode() != HttpStatus.OK) {
				throw new CzmlGenerationException(
						"Cesium service responded with a " + response.getStatusCode()
								.toString());
			}
		}
		catch (final Exception e) {
			throw new CzmlGenerationException(
					e.getMessage());
		}
	}

	NameList getSpaceAssetNames() {
		final ResponseEntity<NameList> response = restTemplate.getForEntity(assetGetSpaceAssets,
																			NameList.class);

		return response.getBody();
	}

	IdList getSpaceAssetsIDs() {
		final ResponseEntity<IdList> response = restTemplate.getForEntity(	assetGetSpaceAssetsIDs,
																			IdList.class);
		return response.getBody();
	}

	SpaceAssetModel getSpaceAssetModel(
			String id ) {
		HashMap<String, String> param = new HashMap<>();
		param.put(	ID_PARAMETER,
					id);
		final ResponseEntity<SpaceAssetModel> response = restTemplate.getForEntity(	assetGetSpaceAssetModelWithID,
																					SpaceAssetModel.class,
																					param);
		return response.getBody();

	}

	/**
	 * Get the SCN of a space asset by the asset's name
	 *
	 * @param name
	 *            The name of the asset.
	 * @return The SCN of the space asset
	 */
	Integer getSpaceAssetIdByName(
			final String name ) {
		try {
			final ResponseEntity<Integer> response = restTemplate.getForEntity(	assetGetSpaceAssetIdByName,
																				Integer.class,
																				name);
			return response.getBody();
		}
		catch (RestClientException e) {
			return 0;
		}
	}

	/**
	 * Get the ephemerides of an space asset by SCN
	 *
	 * @param scn
	 *            The SCN of the asset
	 * @param page
	 *            The number of pages requested for display of the asset ephemerides
	 * @param count
	 *            The number of ephemerides to get for the asset
	 * @return The ephemerides of the asset
	 */
	SpaceObject getSpaceAssetEphermeridesByScn(
			final Integer scn,
			final Integer page,
			final Integer count ) {
		final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(catalogGetAssetEphermeridesByScn + scn);
		builder.queryParam(	"page",
							page)
				.queryParam("count",
							count);
		final ResponseEntity<JsonNode> response = restTemplate.getForEntity(builder.build()
				.encode()
				.toUri(),
																			JsonNode.class);

		final SpaceObject spaceObjectToReturn = new SpaceObject();
		if (response.getBody() == null) {
			return spaceObjectToReturn;
		}
		spaceObjectToReturn.setScn(response.getBody()
				.get("scn")
				.asInt());

		final JsonNode arrNode = response.getBody()
				.withArray("ephemerides");

		final List<EphemerisModel> ephemeridesToReturn = new ArrayList<>();
		for (int i = 0; i < arrNode.size(); i++) {
			final JsonNode currentEphemeris = arrNode.get(i);
			if ((EphemerisType.TLE.toString()).equals(currentEphemeris.get("type")
					.asText())) {
				try {
					ephemeridesToReturn.add(mapper.readValue(	mapper.writeValueAsString(currentEphemeris),
																TLEModel.class));
				}
				catch (IOException e) {
					return spaceObjectToReturn;
				}
			}
		}
		spaceObjectToReturn.setEphemerides(ephemeridesToReturn);
		return spaceObjectToReturn;
	}

	List<String> getAccessConstraintNames() {
		final ParameterizedTypeReference<List<String>> typeReference = new ParameterizedTypeReference<>() {};
		final ResponseEntity<List<String>> response = restTemplate.exchange(accessGetAccessConstraintNamesUrl,
																			HttpMethod.GET,
																			null,
																			typeReference);

		return response.getBody();
	}

	/**
	 * Retrieves the list of access details for access and time specified in an
	 * AccessDetailsRequest
	 *
	 * @param accessDetailsRequest - contains all input params
	 *
	 * @return AccessValues - The access details for the request
	 */
	AccessValues getAccessDetails(
			final AccessDetailsRequest accessDetailsRequest ) {
		final DateTime atTime = DateTime.parse(accessDetailsRequest.getAtTime());
		return getSpaceAccessDetails(	accessDetailsRequest.getAssetId(),
										atTime,
										accessDetailsRequest.getPropagatorType(),
										accessDetailsRequest.getSensorModeName(),
										accessDetailsRequest.getGeometry());
	}

	private static UriComponentsBuilder putAccessParameters(
			final UriComponentsBuilder builder,
			final String assetName,
			final DateTime atTime,
			final String sensorMode,
			final String geometryWkt ) {
		return builder.queryParam(	"assetID",
									assetName)
				.queryParam(DATE_PARAMETER,
							atTime.toString())
				.queryParam("sensorMode",
							sensorMode)
				.queryParam("tgtOrGeometryWKT",
							geometryWkt);
	}

}