package com.maxar.workflow.service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxar.access.model.Access;
import com.maxar.access.model.AccessConstraint;
import com.maxar.access.model.AccessValues;
import com.maxar.access.model.UntrimmedAccess;
import com.maxar.asset.model.IdList;
import com.maxar.asset.model.NameList;
import com.maxar.asset.model.SpaceAssetModel;
import com.maxar.common.exception.BadRequestException;
import com.maxar.spaceobjectcatalog.model.SpaceObject;
import com.maxar.target.model.TargetModel;
import com.maxar.weather.model.weather.WeatherByDateAndGeometryRequest;
import com.maxar.workflow.exception.CzmlGenerationException;
import com.maxar.workflow.model.AccessDetailsRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testworkflowms.properties")
public class ApiServiceTest
{
	private static final String EXAMPLE_ID = "id0";

	private static final String EXAMPLE_GEOMETRY_WKT = "POLYGON ((29.70703125 24.407137917727667, "
			+ "31.596679687499996 24.407137917727667, 31.596679687499996 26.293415004265796, "
			+ "29.70703125 26.293415004265796, 29.70703125 24.407137917727667))";

	private static final String EXAMPLE_START_TIME = "2019-06-03T19:22:00";

	private static final String EXAMPLE_END_TIME = "2019-06-03T19:25:02";

	private static final String EXAMPLE_TCA_TIME = "2019-06-03T19:23:02";

	private static final String EXAMPLE_SESSION = "session0";

	private static final String ACCESS_CONSTRAINT_TYPE_WEATHER = "Weather";

	private static final String EXAMPLE_MODEL_XML = "not real XML";

	private static final double ACCESS_CONSTRAINT_MAX_WEATHER = 0.2;

	@Autowired
	private ApiService apiService;

	@MockBean
	private RestTemplate restTemplate;

	@Value("${microservices.workflow.access.getAccesses}")
	private String accessGetAccessesUrl;

	@Value("${microservices.workflow.weather.getWeatherByDateAndGeometry}")
	private String weatherByDateAndGeometryUrl;

	@Value("${microservices.workflow.asset.getSpaceSmears}")
	private String spaceSmearsUrl;

	@Value("${microservices.workflow.opgen.getSpaceOps}")
	private String spaceOpsUrl;

	@Test
	public void testGetTargetsByGeometry() {
		final TargetModel targetModel = TargetModel.builder()
				.targetId(EXAMPLE_ID)
				.estimated(true)
				.build();

		Mockito.when(restTemplate.exchange(	Mockito.any(URI.class),
											Mockito.eq(HttpMethod.GET),
											Mockito.isNull(),
											(ParameterizedTypeReference<List<TargetModel>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(targetModel)));

		final List<TargetModel> result = apiService.getTargetsByGeometry(EXAMPLE_GEOMETRY_WKT);

		Assert.assertNotNull(result);
		Assert.assertEquals(1,
							result.size());
		Assert.assertEquals(EXAMPLE_ID,
							result.get(0)
									.getTargetId());
		Assert.assertTrue(result.get(0)
				.isEstimated());
	}

	@Test
	public void testGetTargetsByGeometryBadRequest() {
		Mockito.when(restTemplate.exchange(	Mockito.any(URI.class),
											Mockito.eq(HttpMethod.GET),
											Mockito.isNull(),
											(ParameterizedTypeReference<List<TargetModel>>) Mockito.any()))
				.thenReturn(ResponseEntity.badRequest()
						.build());

		final List<TargetModel> result = apiService.getTargetsByGeometry(EXAMPLE_GEOMETRY_WKT);

		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void testGetTargetById() {
		final TargetModel targetModel = TargetModel.builder()
				.targetId(EXAMPLE_ID)
				.estimated(false)
				.build();

		Mockito.when(restTemplate.getForEntity(	Mockito.anyString(),
												Mockito.eq(TargetModel.class),
												(Map<String, String>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(targetModel));

		final Optional<TargetModel> result = apiService.getTargetById(EXAMPLE_ID);

		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(EXAMPLE_ID,
							result.get()
									.getTargetId());
		Assert.assertFalse(result.get()
				.isEstimated());
	}

	@Test
	public void testGetTargetByIdNotFound() {
		Mockito.when(restTemplate.getForEntity(	Mockito.anyString(),
												Mockito.eq(TargetModel.class),
												(Map<String, String>) Mockito.any()))
				.thenReturn(ResponseEntity.notFound()
						.build());

		final Optional<TargetModel> result = apiService.getTargetById(EXAMPLE_ID);

		Assert.assertFalse(result.isPresent());
	}

	@Test
	public void testGetSpaceAccessesByGeometryAndTimes()
			throws IOException {
		final UntrimmedAccess untrimmedAccess = new UntrimmedAccess();
		untrimmedAccess.setAssetID(EXAMPLE_ID);
		untrimmedAccess.setStartTimeISO8601(EXAMPLE_START_TIME);
		untrimmedAccess.setEndTimeISO8601(EXAMPLE_END_TIME);
		untrimmedAccess.setTcaTimeISO8601(EXAMPLE_TCA_TIME);

		final List<UntrimmedAccess> untrimmedAccesses = new ArrayList<>();
		untrimmedAccesses.add(untrimmedAccess);
		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<UntrimmedAccess>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(untrimmedAccesses));

		final JsonNode accessCzmlJsonNode = stringToJsonNode("{\"id\":\"Space Accesses\",\"name\":\"Space Accesses\"}");
		final List<JsonNode> accessCzmlJsonNodes = new ArrayList<>();
		accessCzmlJsonNodes.add(accessCzmlJsonNode);
		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(accessCzmlJsonNodes));

		final JsonNode weatherCzmlJsonNode = stringToJsonNode("{\"parent\":\"Space Accesses\"}");
		final List<JsonNode> weatherCzmlJsonNodes = new ArrayList<>();
		weatherCzmlJsonNodes.add(weatherCzmlJsonNode);
		Mockito.when(restTemplate.exchange(	Mockito.eq(weatherByDateAndGeometryUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(weatherCzmlJsonNodes));

		Mockito.when(restTemplate.exchange(	Mockito.eq(spaceSmearsUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		Mockito.when(restTemplate.exchange(	Mockito.eq(spaceOpsUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final List<UntrimmedAccess> result = apiService.getSpaceAccessesByGeometryAndTimes(	EXAMPLE_GEOMETRY_WKT,
																							EXAMPLE_START_TIME,
																							EXAMPLE_END_TIME,
																							Collections
																									.singletonList(EXAMPLE_ID),
																							Collections
																									.singletonList(accessConstraint),
																							null,
																							null);

		Assert.assertNotNull(result);
		Assert.assertEquals(1,
							result.size());
		Assert.assertEquals(EXAMPLE_ID,
							result.get(0)
									.getAssetID());
		Assert.assertNotNull(result.get(0)
				.getCzml());
		Assert.assertEquals(2,
							result.get(0)
									.getCzml()
									.size());
		Assert.assertEquals(accessCzmlJsonNode.toString(),
							result.get(0)
									.getCzml()
									.get(0)
									.toString());
		Assert.assertEquals(weatherCzmlJsonNode.toString(),
							result.get(0)
									.getCzml()
									.get(1)
									.toString());
	}

	@Test
	public void testGetSpaceAccessesByGeometryAndTimesWithTrimmed()
			throws IOException {
		final UntrimmedAccess untrimmedAccess = new UntrimmedAccess();
		untrimmedAccess.setAssetID(EXAMPLE_ID);
		untrimmedAccess.setStartTimeISO8601(EXAMPLE_START_TIME);
		untrimmedAccess.setEndTimeISO8601(EXAMPLE_END_TIME);
		untrimmedAccess.setTcaTimeISO8601(EXAMPLE_TCA_TIME);

		final Access trimmed = new Access();
		trimmed.setStartTimeISO8601(EXAMPLE_START_TIME);
		trimmed.setEndTimeISO8601(EXAMPLE_END_TIME);
		trimmed.setTcaTimeISO8601(EXAMPLE_TCA_TIME);

		untrimmedAccess.setTrimmedAccesses(Collections.singletonList(trimmed));

		final List<UntrimmedAccess> untrimmedAccesses = new ArrayList<>();
		untrimmedAccesses.add(untrimmedAccess);
		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<UntrimmedAccess>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(untrimmedAccesses));

		final JsonNode accessCzmlJsonNode = stringToJsonNode("{\"id\":\"Space Accesses\",\"name\":\"Space Accesses\"}");
		final List<JsonNode> accessCzmlJsonNodes = new ArrayList<>();
		accessCzmlJsonNodes.add(accessCzmlJsonNode);
		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(accessCzmlJsonNodes));

		final JsonNode weatherCzmlJsonNode = stringToJsonNode("{\"parent\":\"Space Accesses\"}");
		final List<JsonNode> weatherCzmlJsonNodes = new ArrayList<>();
		weatherCzmlJsonNodes.add(weatherCzmlJsonNode);
		Mockito.when(restTemplate.exchange(	Mockito.eq(weatherByDateAndGeometryUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(weatherCzmlJsonNodes));

		Mockito.when(restTemplate.exchange(	Mockito.eq(spaceSmearsUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		Mockito.when(restTemplate.exchange(	Mockito.eq(spaceOpsUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final List<UntrimmedAccess> result = apiService.getSpaceAccessesByGeometryAndTimes(	EXAMPLE_GEOMETRY_WKT,
																							EXAMPLE_START_TIME,
																							EXAMPLE_END_TIME,
																							Collections
																									.singletonList(EXAMPLE_ID),
																							Collections
																									.singletonList(accessConstraint),
																							null,
																							null);

		Assert.assertNotNull(result);
		Assert.assertEquals(1,
							result.size());
		Assert.assertEquals(EXAMPLE_ID,
							result.get(0)
									.getAssetID());
		Assert.assertNotNull(result.get(0)
				.getCzml());
		Assert.assertEquals(2,
							result.get(0)
									.getCzml()
									.size());
		Assert.assertEquals(accessCzmlJsonNode.toString(),
							result.get(0)
									.getCzml()
									.get(0)
									.toString());
		Assert.assertEquals(weatherCzmlJsonNode.toString(),
							result.get(0)
									.getCzml()
									.get(1)
									.toString());
		Assert.assertNotNull(result.get(0)
				.getTrimmedAccesses());
		Assert.assertEquals(1,
							result.get(0)
									.getTrimmedAccesses()
									.size());
		Assert.assertNotNull(result.get(0)
				.getTrimmedAccesses()
				.get(0));
		Assert.assertEquals(EXAMPLE_START_TIME,
							result.get(0)
									.getTrimmedAccesses()
									.get(0)
									.getStartTimeISO8601());
		Assert.assertEquals(EXAMPLE_END_TIME,
							result.get(0)
									.getTrimmedAccesses()
									.get(0)
									.getEndTimeISO8601());
		Assert.assertEquals(EXAMPLE_TCA_TIME,
							result.get(0)
									.getTrimmedAccesses()
									.get(0)
									.getTcaTimeISO8601());
	}

	@Test
	public void testGetSpaceAccessesByGeometryAndTimesBadRequest() {
		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<UntrimmedAccess>>) Mockito.any()))
				.thenReturn(ResponseEntity.badRequest()
						.build());

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final List<UntrimmedAccess> result = apiService.getSpaceAccessesByGeometryAndTimes(	EXAMPLE_GEOMETRY_WKT,
																							EXAMPLE_START_TIME,
																							EXAMPLE_END_TIME,
																							Collections
																									.singletonList(EXAMPLE_ID),
																							Collections
																									.singletonList(accessConstraint),
																							null,
																							null);

		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());
	}

	@Test(expected = BadRequestException.class)
	public void testGetSpaceAccessesByGeometryAndTimesInvalidStartTime() {
		final UntrimmedAccess untrimmedAccess = new UntrimmedAccess();
		untrimmedAccess.setAssetID(EXAMPLE_ID);
		untrimmedAccess.setStartTimeISO8601("abc");
		untrimmedAccess.setEndTimeISO8601(EXAMPLE_END_TIME);
		untrimmedAccess.setTcaTimeISO8601(EXAMPLE_TCA_TIME);

		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<UntrimmedAccess>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(untrimmedAccess)));

		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		apiService.getSpaceAccessesByGeometryAndTimes(	EXAMPLE_GEOMETRY_WKT,
														EXAMPLE_START_TIME,
														EXAMPLE_END_TIME,
														Collections.singletonList(EXAMPLE_ID),
														Collections.singletonList(accessConstraint),
														null,
														null);
	}

	@Test(expected = BadRequestException.class)
	public void testGetSpaceAccessesByGeometryAndTimesInvalidEndTime() {
		final UntrimmedAccess untrimmedAccess = new UntrimmedAccess();
		untrimmedAccess.setAssetID(EXAMPLE_ID);
		untrimmedAccess.setStartTimeISO8601(EXAMPLE_START_TIME);
		untrimmedAccess.setEndTimeISO8601("def");
		untrimmedAccess.setTcaTimeISO8601(EXAMPLE_TCA_TIME);

		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<UntrimmedAccess>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(untrimmedAccess)));

		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		apiService.getSpaceAccessesByGeometryAndTimes(	EXAMPLE_GEOMETRY_WKT,
														EXAMPLE_START_TIME,
														EXAMPLE_END_TIME,
														Collections.singletonList(EXAMPLE_ID),
														Collections.singletonList(accessConstraint),
														null,
														null);
	}

	@Test
	public void testGetSpaceAccessesByGeometryAndTimesWeatherRequestException() {
		final UntrimmedAccess untrimmedAccess = new UntrimmedAccess();
		untrimmedAccess.setAssetID(EXAMPLE_ID);
		untrimmedAccess.setStartTimeISO8601(EXAMPLE_START_TIME);
		untrimmedAccess.setEndTimeISO8601(EXAMPLE_END_TIME);
		untrimmedAccess.setTcaTimeISO8601(EXAMPLE_TCA_TIME);

		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<UntrimmedAccess>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(untrimmedAccess)));

		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		Mockito.when(restTemplate.exchange(	Mockito.eq(weatherByDateAndGeometryUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenThrow(new RuntimeException(
						"error"));

		Mockito.when(restTemplate.exchange(	Mockito.eq(spaceSmearsUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		Mockito.when(restTemplate.exchange(	Mockito.eq(spaceOpsUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final List<UntrimmedAccess> result = apiService.getSpaceAccessesByGeometryAndTimes(	EXAMPLE_GEOMETRY_WKT,
																							EXAMPLE_START_TIME,
																							EXAMPLE_END_TIME,
																							Collections
																									.singletonList(EXAMPLE_ID),
																							Collections
																									.singletonList(accessConstraint),
																							null,
																							null);

		Assert.assertNotNull(result);
		Assert.assertEquals(1,
							result.size());
		Assert.assertEquals(EXAMPLE_ID,
							result.get(0)
									.getAssetID());
		Assert.assertNotNull(result.get(0)
				.getCzml());
		Assert.assertTrue(result.get(0)
				.getCzml()
				.isEmpty());
	}

	@Test
	public void testGetSpaceAccessesByGeometryAndTimesSmearRequestException() {
		final UntrimmedAccess untrimmedAccess = new UntrimmedAccess();
		untrimmedAccess.setAssetID(EXAMPLE_ID);
		untrimmedAccess.setStartTimeISO8601(EXAMPLE_START_TIME);
		untrimmedAccess.setEndTimeISO8601(EXAMPLE_END_TIME);
		untrimmedAccess.setTcaTimeISO8601(EXAMPLE_TCA_TIME);

		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<UntrimmedAccess>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(untrimmedAccess)));

		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		Mockito.when(restTemplate.exchange(	Mockito.eq(weatherByDateAndGeometryUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		Mockito.when(restTemplate.exchange(	Mockito.eq(spaceSmearsUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenThrow(new RuntimeException(
						"error"));

		Mockito.when(restTemplate.exchange(	Mockito.eq(spaceOpsUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final List<UntrimmedAccess> result = apiService.getSpaceAccessesByGeometryAndTimes(	EXAMPLE_GEOMETRY_WKT,
																							EXAMPLE_START_TIME,
																							EXAMPLE_END_TIME,
																							Collections
																									.singletonList(EXAMPLE_ID),
																							Collections
																									.singletonList(accessConstraint),
																							null,
																							null);

		Assert.assertNotNull(result);
		Assert.assertEquals(1,
							result.size());
		Assert.assertEquals(EXAMPLE_ID,
							result.get(0)
									.getAssetID());
		Assert.assertNotNull(result.get(0)
				.getCzml());
		Assert.assertTrue(result.get(0)
				.getCzml()
				.isEmpty());
	}

	@Test
	public void testGetSpaceAccessesByGeometryAndTimesOpsRequestException() {
		final UntrimmedAccess untrimmedAccess = new UntrimmedAccess();
		untrimmedAccess.setAssetID(EXAMPLE_ID);
		untrimmedAccess.setStartTimeISO8601(EXAMPLE_START_TIME);
		untrimmedAccess.setEndTimeISO8601(EXAMPLE_END_TIME);
		untrimmedAccess.setTcaTimeISO8601(EXAMPLE_TCA_TIME);

		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<UntrimmedAccess>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(untrimmedAccess)));

		Mockito.when(restTemplate.exchange(	Mockito.eq(accessGetAccessesUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		Mockito.when(restTemplate.exchange(	Mockito.eq(weatherByDateAndGeometryUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		Mockito.when(restTemplate.exchange(	Mockito.eq(spaceSmearsUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		Mockito.when(restTemplate.exchange(	Mockito.eq(spaceOpsUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenThrow(new RuntimeException(
						"error"));

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final List<UntrimmedAccess> result = apiService.getSpaceAccessesByGeometryAndTimes(	EXAMPLE_GEOMETRY_WKT,
																							EXAMPLE_START_TIME,
																							EXAMPLE_END_TIME,
																							Collections
																									.singletonList(EXAMPLE_ID),
																							Collections
																									.singletonList(accessConstraint),
																							null,
																							null);

		Assert.assertNotNull(result);
		Assert.assertEquals(1,
							result.size());
		Assert.assertEquals(EXAMPLE_ID,
							result.get(0)
									.getAssetID());
		Assert.assertNotNull(result.get(0)
				.getCzml());
		Assert.assertTrue(result.get(0)
				.getCzml()
				.isEmpty());
	}

	@Test
	public void testGetSpaceAccessDetails() {
		final AccessValues accessValues = new AccessValues();

		Mockito.when(restTemplate.getForEntity(	Mockito.any(),
												Mockito.eq(AccessValues.class)))
				.thenReturn(ResponseEntity.ok(accessValues));

		final AccessValues result = apiService.getSpaceAccessDetails(	"asset0",
																		DateTime.parse(EXAMPLE_START_TIME),
																		"J2",
																		"mode0",
																		EXAMPLE_ID);

		Assert.assertNotNull(result);
	}

	@Test
	public void testGetSpaceAccessDetailsBadRequest() {
		Mockito.when(restTemplate.getForEntity(	Mockito.any(),
												Mockito.eq(AccessValues.class)))
				.thenReturn(ResponseEntity.badRequest()
						.build());

		final AccessValues result = apiService.getSpaceAccessDetails(	"asset0",
																		DateTime.parse(EXAMPLE_START_TIME),
																		"J2",
																		"mode0",
																		EXAMPLE_ID);

		Assert.assertNull(result);
	}

	@Test
	public void testDeleteCzml()
			throws CzmlGenerationException {
		Mockito.doNothing()
				.when(restTemplate)
				.delete(Mockito.anyString());

		apiService.deleteCzml(EXAMPLE_SESSION);

		Assert.assertTrue(true);
	}

	@Test(expected = CzmlGenerationException.class)
	public void testDeleteCzmlFailure()
			throws CzmlGenerationException {
		Mockito.doThrow(new RuntimeException(
				"error"))
				.when(restTemplate)
				.delete(Mockito.anyString());

		apiService.deleteCzml(EXAMPLE_SESSION);
	}

	@Test
	public void testDisplayCzml()
			throws CzmlGenerationException {
		Mockito.when(restTemplate.postForEntity(Mockito.anyString(),
												Mockito.any(),
												Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok()
						.build());

		apiService.displayCzml(	EXAMPLE_SESSION,
								Collections.emptyList());

		Assert.assertTrue(true);
	}

	@Test(expected = CzmlGenerationException.class)
	public void testDisplayCzmlBadRequest()
			throws CzmlGenerationException {
		Mockito.when(restTemplate.postForEntity(Mockito.anyString(),
												Mockito.any(),
												Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.badRequest()
						.build());

		apiService.displayCzml(	EXAMPLE_SESSION,
								Collections.emptyList());
	}

	@Test
	public void testGetSpaceAssetNames() {
		final NameList nameList = new NameList();
		nameList.setNames(Collections.emptyList());

		Mockito.when(restTemplate.getForEntity(	Mockito.anyString(),
												Mockito.eq(NameList.class)))
				.thenReturn(ResponseEntity.ok(nameList));

		final NameList result = apiService.getSpaceAssetNames();

		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getNames());
		Assert.assertTrue(result.getNames()
				.isEmpty());
	}

	@Test
	public void testGetSpaceAssetIDs() {
		final IdList idList = new IdList();
		idList.setIds(Collections.emptyList());

		Mockito.when(restTemplate.getForEntity(	Mockito.anyString(),
												Mockito.eq(IdList.class)))
				.thenReturn(ResponseEntity.ok(idList));

		final IdList result = apiService.getSpaceAssetsIDs();

		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getIds());
		Assert.assertTrue(result.getIds()
				.isEmpty());
	}

	@Test
	public void testGetSpaceAssetModel() {
		final SpaceAssetModel spaceAssetModel = new SpaceAssetModel();
		spaceAssetModel.setModelXml(EXAMPLE_MODEL_XML);

		Mockito.when(restTemplate.getForEntity(	Mockito.anyString(),
												Mockito.eq(SpaceAssetModel.class),
												Mockito.anyMap()))
				.thenReturn(ResponseEntity.ok(spaceAssetModel));

		final SpaceAssetModel result = apiService.getSpaceAssetModel(EXAMPLE_ID);

		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getModelXml());
		Assert.assertEquals(EXAMPLE_MODEL_XML,
							result.getModelXml());
	}

	private static JsonNode stringToJsonNode(
			final String jsonString )
			throws IOException {
		final ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readTree(jsonString);
	}

	@Test
	public void testGenerateCzmlForTrimmedAccess()
			throws IOException {

		final UntrimmedAccess untrimmed = new UntrimmedAccess();
		untrimmed.setAssetID(EXAMPLE_ID);
		untrimmed.setStartTimeISO8601(EXAMPLE_START_TIME);
		untrimmed.setEndTimeISO8601("def");
		untrimmed.setTcaTimeISO8601(EXAMPLE_TCA_TIME);
		untrimmed.setAssetName("Asset0");
		untrimmed.setSensorMode("SensorMode");
		untrimmed.setPropagatorType("PropagatorType");

		final Access trimmed = new Access();
		trimmed.setStartTimeISO8601(EXAMPLE_START_TIME);
		trimmed.setEndTimeISO8601(EXAMPLE_END_TIME);

		final JsonNode commonCzmlJsonNode = stringToJsonNode("{\"id\":\"Space Accesses\",\"name\":\"Space Accesses\"}");
		final List<JsonNode> commonCzmlJsonNodes = new ArrayList<>();
		commonCzmlJsonNodes.add(commonCzmlJsonNode);

		final JsonNode weatherCzmlJsonNode = stringToJsonNode("{\"id\":\"Weather\",\"name\":\"Weather\"}");
		final List<JsonNode> weatherCzmlJsonNodes = new ArrayList<>();
		weatherCzmlJsonNodes.add(weatherCzmlJsonNode);

		final JsonNode fovSmearCzmlJsonNode = stringToJsonNode("{\"id\":\"FOV_Smear\",\"name\":\"FOV_Smear\"}");
		final List<JsonNode> fovSmearCzmlJsonNodes = new ArrayList<>();
		fovSmearCzmlJsonNodes.add(fovSmearCzmlJsonNode);

		final JsonNode opCzmlJsonNode = stringToJsonNode("{\"id\":\"FOV_Smear\",\"name\":\"FOV_Smear\"}");
		final List<JsonNode> opCzmlJsonNodes = new ArrayList<>();
		opCzmlJsonNodes.add(opCzmlJsonNode);

		Mockito.when(restTemplate.exchange(	Mockito.eq(weatherByDateAndGeometryUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(weatherCzmlJsonNodes));

		Mockito.when(restTemplate.exchange(	Mockito.eq(spaceSmearsUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(fovSmearCzmlJsonNodes));

		Mockito.when(restTemplate.exchange(	Mockito.eq(spaceOpsUrl + "?format=czml"),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<JsonNode>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(opCzmlJsonNodes));

		final List<JsonNode> result = apiService.generateCzmlForTrimmedAccess(	commonCzmlJsonNodes,
																				trimmed,
																				untrimmed,
																				EXAMPLE_GEOMETRY_WKT,
																				EXAMPLE_ID);

		Assert.assertNotNull(result);

		Assert.assertEquals(4,
							result.size());
		Assert.assertEquals(commonCzmlJsonNode.toString(),
							result.get(0)
									.toString());
		Assert.assertEquals(weatherCzmlJsonNode.toString(),
							result.get(1)
									.toString());
		Assert.assertEquals(fovSmearCzmlJsonNode.toString(),
							result.get(2)
									.toString());
		Assert.assertEquals(opCzmlJsonNode.toString(),
							result.get(3)
									.toString());

	}

	@Test(expected = BadRequestException.class)
	public void testGenerateCzmlForTrimmedAccessStartTimeInvalid()
			throws IOException {
		final UntrimmedAccess untrimmed = new UntrimmedAccess();
		untrimmed.setAssetID(EXAMPLE_ID);
		untrimmed.setStartTimeISO8601(EXAMPLE_START_TIME);
		untrimmed.setEndTimeISO8601("def");
		untrimmed.setTcaTimeISO8601(EXAMPLE_TCA_TIME);
		untrimmed.setAssetName("Asset0");
		untrimmed.setSensorMode("SensorMode");
		untrimmed.setPropagatorType("PropagatorType");

		final Access trimmed = new Access();
		trimmed.setStartTimeISO8601("not a real time");
		trimmed.setEndTimeISO8601(EXAMPLE_END_TIME);

		final JsonNode commonCzmlJsonNode = stringToJsonNode("{\"id\":\"Space Accesses\",\"name\":\"Space Accesses\"}");
		final List<JsonNode> commonCzmlJsonNodes = new ArrayList<>();
		commonCzmlJsonNodes.add(commonCzmlJsonNode);

		apiService.generateCzmlForTrimmedAccess(commonCzmlJsonNodes,
												trimmed,
												untrimmed,
												EXAMPLE_GEOMETRY_WKT,
												EXAMPLE_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testGenerateCzmlForTrimmedAccessEndTimeInvalid()
			throws IOException {
		final UntrimmedAccess untrimmed = new UntrimmedAccess();
		untrimmed.setAssetID(EXAMPLE_ID);
		untrimmed.setStartTimeISO8601(EXAMPLE_START_TIME);
		untrimmed.setEndTimeISO8601("def");
		untrimmed.setTcaTimeISO8601(EXAMPLE_TCA_TIME);
		untrimmed.setAssetName("Asset0");
		untrimmed.setSensorMode("SensorMode");
		untrimmed.setPropagatorType("PropagatorType");

		final Access trimmed = new Access();
		trimmed.setStartTimeISO8601(EXAMPLE_START_TIME);
		trimmed.setEndTimeISO8601("invalid time");

		final JsonNode commonCzmlJsonNode = stringToJsonNode("{\"id\":\"Space Accesses\",\"name\":\"Space Accesses\"}");
		final List<JsonNode> commonCzmlJsonNodes = new ArrayList<>();
		commonCzmlJsonNodes.add(commonCzmlJsonNode);

		apiService.generateCzmlForTrimmedAccess(commonCzmlJsonNodes,
												trimmed,
												untrimmed,
												EXAMPLE_GEOMETRY_WKT,
												EXAMPLE_ID);
	}

	@Test
	public void testGetAccessWeather() {

		final WeatherByDateAndGeometryRequest weatherRequest = new WeatherByDateAndGeometryRequest();

		weatherRequest.setDateTimeISO8601(EXAMPLE_START_TIME);
		weatherRequest.setGeometryWKT(EXAMPLE_GEOMETRY_WKT);

		Mockito.when(restTemplate.getForEntity(	ArgumentMatchers.any(URI.class),
												Mockito.eq(Double.class)))
				.thenReturn(ResponseEntity.ok(25.0));

		final Double cloudCoverResult = apiService.getAccessWeather(weatherRequest);

		Assert.assertNotNull(cloudCoverResult);
		Assert.assertEquals(25.0,
							cloudCoverResult,
							0.001);

	}

	@Test
	public void testGetSpaceAssetIdByName() {

		Mockito.when(restTemplate.getForEntity(	Mockito.anyString(),
												Mockito.eq(Integer.class),
												Mockito.anyString()))
				.thenReturn(ResponseEntity.ok(1));

		final Integer scn = apiService.getSpaceAssetIdByName("asset01");

		Assert.assertNotNull(scn);
		Assert.assertEquals((Integer) 1,
							scn);
	}

	@Test
	public void testGetSpaceAssetEphermeridesByScn()
			throws IOException {

		final SpaceObject spaceObject = new SpaceObject();
		spaceObject.setScn(1);

		final JsonNode jsonNodeResult = stringToJsonNode("{\"scn\":1,\"ephemerides\":[{  \"scn\": 1,\n"
				+ "  \"type\": \"TLE\",\n" + "  \"epochMillis\": 15,\n" + "  \"description\": \"R\",\n"
				+ "  \"tleLineOne\": \"1\",\n" + "  \"tleLineTwo\": \"2\"}]}");

		Mockito.doReturn(ResponseEntity.ok(jsonNodeResult))
				.when(restTemplate)
				.getForEntity(	Mockito.any(URI.class),
								Mockito.eq(JsonNode.class));

		final SpaceObject result = apiService.getSpaceAssetEphermeridesByScn(	1,
																				1,
																				1);
		Assert.assertNotNull(result);
		Assert.assertEquals((Integer) 1,
							result.getScn());
		Assert.assertNotNull(result.getEphemerides());
		Assert.assertEquals("TLE",
							result.getEphemerides()
									.get(0)
									.getType()
									.toString());
	}

	@Test
	public void testGetAccessConstraintNames() {
		Mockito.when(restTemplate.exchange(	Mockito.anyString(),
											Mockito.eq(HttpMethod.GET),
											Mockito.isNull(),
											(ParameterizedTypeReference<List<String>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(ACCESS_CONSTRAINT_TYPE_WEATHER)));

		final List<String> result = apiService.getAccessConstraintNames();

		Assert.assertNotNull(result);
		Assert.assertEquals(1,
							result.size());
		Assert.assertEquals(ACCESS_CONSTRAINT_TYPE_WEATHER,
							result.get(0));
	}

	@Test
	public void testGetAccessDetails() {

		final AccessDetailsRequest accessDetailsRequest = new AccessDetailsRequest();

		accessDetailsRequest.setAtTime(EXAMPLE_START_TIME);
		accessDetailsRequest.setGeometry(EXAMPLE_GEOMETRY_WKT);
		accessDetailsRequest.setPropagatorType("SGP4");
		accessDetailsRequest.setAssetId(EXAMPLE_ID);
		accessDetailsRequest.setSensorModeName(EXAMPLE_ID + "_mode");

		Mockito.when(restTemplate.getForEntity(	ArgumentMatchers.any(URI.class),
												Mockito.eq(AccessValues.class)))
				.thenReturn(ResponseEntity.ok(new AccessValues()));

		final AccessValues accessValues = apiService.getAccessDetails(accessDetailsRequest);

		Assert.assertNotNull(accessValues);

	}
}
