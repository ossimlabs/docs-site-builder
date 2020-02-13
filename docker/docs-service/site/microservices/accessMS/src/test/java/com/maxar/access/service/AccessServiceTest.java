package com.maxar.access.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxar.access.common.service.AccessService;
import com.maxar.access.common.utils.AssetUtils;
import com.maxar.access.common.utils.ConstraintUtils;
import com.maxar.access.model.AccessConstraint;
import com.maxar.access.model.AccessValues;
import com.maxar.access.model.UntrimmedAccess;
import com.maxar.asset.common.exception.NoEphemerisFoundException;
import com.maxar.asset.common.exception.PropagatorTypeDoesNotExistException;
import com.maxar.target.model.OrderOfBattle;
import com.maxar.target.model.TargetType;
import com.maxar.terrain.model.GoodTimeIntervalsResponse;
import com.maxar.terrain.model.TimeInterval;
import com.radiantblue.analytics.core.constraint.IAccessConstraint;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.isr.core.model.sensormode.ISensorMode;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testaccessms.properties")
public class AccessServiceTest
{
	private final static String GEOMETRY_WKT = "POINT(-77.38368 38.96686 135.0)";

	private final static String START_TIME = "2020-01-01T09:00:00.000Z";

	private final static String END_TIME = "2020-01-01T17:00:00.000Z";

	private final static String SENSOR_TYPE = "RADAR";

	private final static String SENSOR_MODE = "RS02_Framing_Mode";

	private final static String PROPAGATOR_TYPE = "SGP4";

	private final static TargetType TARGET_TYPE = TargetType.POINT;

	private final static OrderOfBattle ORDER_OF_BATTLE = OrderOfBattle.GROUND;

	private final static String TLE_MODEL_JSON_NODE_STRING = "{" + "\"scn\": 32382," + "\"type\": \"TLE\","
			+ "\"epochMillis\": 1577776254641," + "\"description\": \"RADARSAT-2\","
			+ "\"tleLineOne\": \"1 32382U 07061A   19365.29924354  .00000214  00000-0  10000-3 0  9991\","
			+ "\"tleLineTwo\": \"2 32382  98.5777 267.8620 0001184  71.2166  55.0008 14.29985561614077\"" + "}";

	private static JsonNode tleJsonNodeResponse;

	@Autowired
	private AccessService accessService;

	@Autowired
	private ConstraintUtils constraintUtils;

	@Autowired
	private AssetUtils assetUtils;

	@MockBean
	private RestTemplate restTemplate;

	@Value("${microservices.access.terrainUrl}")
	private String terrainUrl;

	@BeforeClass
	public static void setUp()
			throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonFactory factory = mapper.getFactory();
		final JsonParser parser = factory.createParser(TLE_MODEL_JSON_NODE_STRING);
		tleJsonNodeResponse = mapper.readTree(parser);
	}

	@Test
	public void testGetAccesses()
			throws ParseException,
			IOException {
		Mockito.when(restTemplate.getForEntity(	Mockito.any(URI.class),
		                                       	Mockito.eq(JsonNode.class)))
				.thenReturn(ResponseEntity.ok(tleJsonNodeResponse));

		final DateTime startTime = DateTime.parse(START_TIME);
		final DateTime endTime = DateTime.parse(END_TIME);
		final Geometry geometry = parseGeometry();

		final Asset asset = buildRS02();
		Assert.assertNotNull(asset);

		final AccessConstraint qual = new AccessConstraint();
		qual.setName(ConstraintUtils.qualityConstraintName);
		qual.setMinValue(4.0);
		final IAccessConstraint qualityConstraint = constraintUtils.buildConstraint(qual);
		Assert.assertNotNull(qualityConstraint);

		final List<UntrimmedAccess> accesses = accessService.getAccesses(	Collections.singletonList(asset),
																			startTime,
																			endTime,
																			Collections
																					.singletonList(qualityConstraint),
																			SENSOR_TYPE,
																			geometry,
																			TARGET_TYPE,
																			PROPAGATOR_TYPE,
																			ORDER_OF_BATTLE);

		Assert.assertNotNull(accesses);

		Assert.assertFalse(accesses.isEmpty());
	}

	@Test
	public void testGetAccessesNoEphemeris()
			throws ParseException,
			IOException {
		Mockito.when(restTemplate.getForEntity(	Mockito.any(URI.class),
		                                       	Mockito.eq(JsonNode.class)))
				.thenReturn(ResponseEntity.notFound()
						.build());

		final DateTime startTime = DateTime.parse(START_TIME);
		final DateTime endTime = DateTime.parse(END_TIME);
		final Geometry geometry = parseGeometry();

		final Asset asset = buildRS02();
		Assert.assertNotNull(asset);

		final AccessConstraint qual = new AccessConstraint();
		qual.setName(ConstraintUtils.qualityConstraintName);
		qual.setMinValue(4.0);
		final IAccessConstraint qualityConstraint = constraintUtils.buildConstraint(qual);
		Assert.assertNotNull(qualityConstraint);

		final List<UntrimmedAccess> accesses = accessService.getAccesses(	Collections.singletonList(asset),
																			startTime,
																			endTime,
																			Collections
																					.singletonList(qualityConstraint),
																			SENSOR_TYPE,
																			geometry,
																			TARGET_TYPE,
																			PROPAGATOR_TYPE,
																			ORDER_OF_BATTLE);

		Assert.assertNotNull(accesses);

		Assert.assertTrue(accesses.isEmpty());
	}

	@Test
	public void testGetAccessesWithTerrainAndWeatherConstraints()
			throws ParseException,
			IOException {
		Mockito.when(restTemplate.getForEntity(	Mockito.any(URI.class),
		                                       	Mockito.eq(JsonNode.class)))
				.thenReturn(ResponseEntity.ok(tleJsonNodeResponse));

		final DateTime startTime = DateTime.parse(START_TIME);
		final DateTime endTime = DateTime.parse(END_TIME);

		final TimeInterval timeInterval = new TimeInterval();
		timeInterval.setStart(startTime);
		timeInterval.setEnd(endTime);

		final GoodTimeIntervalsResponse goodTimeIntervalsResponse = new GoodTimeIntervalsResponse();
		goodTimeIntervalsResponse.setTimeIntervals(Collections.singletonList(timeInterval));

		Mockito.when(restTemplate.postForEntity(ArgumentMatchers.eq(terrainUrl),
												ArgumentMatchers.any(),
												ArgumentMatchers.eq(GoodTimeIntervalsResponse.class)))
				.thenReturn(ResponseEntity.ok(goodTimeIntervalsResponse));

		Mockito.when(restTemplate.getForEntity(	ArgumentMatchers.any(URI.class),
												ArgumentMatchers.eq(Double.class)))
				.thenReturn(ResponseEntity.ok(0.0));

		final Geometry geometry = parseGeometry();

		final Asset asset = buildRS02();
		Assert.assertNotNull(asset);

		final AccessConstraint qual = new AccessConstraint();
		qual.setName(ConstraintUtils.qualityConstraintName);
		qual.setMinValue(4.0);
		final IAccessConstraint qualityConstraint = constraintUtils.buildConstraint(qual);
		Assert.assertNotNull(qualityConstraint);

		final AccessConstraint terrain = new AccessConstraint();
		terrain.setName(ConstraintUtils.terrainConstraintName);
		final IAccessConstraint terrainConstraint = constraintUtils.buildConstraint(terrain);
		Assert.assertNotNull(terrainConstraint);

		final AccessConstraint weather = new AccessConstraint();
		weather.setName(ConstraintUtils.weatherConstraintName);
		weather.setMaxValue(0.3);
		final IAccessConstraint weatherConstraint = constraintUtils.buildConstraint(weather);
		Assert.assertNotNull(weatherConstraint);

		final List<IAccessConstraint> constraints = Arrays.asList(	qualityConstraint,
																	terrainConstraint,
																	weatherConstraint);

		final List<UntrimmedAccess> accesses = accessService.getAccesses(	Collections.singletonList(asset),
																			startTime,
																			endTime,
																			constraints,
																			SENSOR_TYPE,
																			geometry,
																			TARGET_TYPE,
																			PROPAGATOR_TYPE,
																			ORDER_OF_BATTLE);

		Assert.assertNotNull(accesses);

		Assert.assertFalse(accesses.isEmpty());
	}

	@Test
	public void testGetAccessesWithTerrainAndWeatherConstraintsMultipleGoodIntervals()
			throws ParseException,
			IOException {
		Mockito.when(restTemplate.getForEntity(	Mockito.any(URI.class),
		                                       	Mockito.eq(JsonNode.class)))
				.thenReturn(ResponseEntity.ok(tleJsonNodeResponse));

		final DateTime startTime = DateTime.parse(START_TIME);
		final DateTime endTime = DateTime.parse(END_TIME);

		final TimeInterval timeInterval1 = new TimeInterval();
		timeInterval1.setStart(startTime);
		timeInterval1.setEnd(DateTime.parse("2020-01-01T12:00:00Z"));

		final TimeInterval timeInterval2 = new TimeInterval();
		timeInterval2.setStart(DateTime.parse("2020-01-01T15:00:00Z"));
		timeInterval2.setEnd(endTime);

		final GoodTimeIntervalsResponse goodTimeIntervalsResponse = new GoodTimeIntervalsResponse();
		goodTimeIntervalsResponse.setTimeIntervals(Arrays.asList(	timeInterval1,
																	timeInterval2));

		Mockito.when(restTemplate.postForEntity(ArgumentMatchers.eq(terrainUrl),
												ArgumentMatchers.any(),
												ArgumentMatchers.eq(GoodTimeIntervalsResponse.class)))
				.thenReturn(ResponseEntity.ok(goodTimeIntervalsResponse));

		Mockito.when(restTemplate.getForEntity(	ArgumentMatchers.any(URI.class),
												ArgumentMatchers.eq(Double.class)))
				.thenReturn(ResponseEntity.ok(0.0));

		final Geometry geometry = parseGeometry();

		final Asset asset = buildRS02();
		Assert.assertNotNull(asset);

		final AccessConstraint qual = new AccessConstraint();
		qual.setName(ConstraintUtils.qualityConstraintName);
		qual.setMinValue(4.0);
		final IAccessConstraint qualityConstraint = constraintUtils.buildConstraint(qual);
		Assert.assertNotNull(qualityConstraint);

		final AccessConstraint terrain = new AccessConstraint();
		terrain.setName(ConstraintUtils.terrainConstraintName);
		final IAccessConstraint terrainConstraint = constraintUtils.buildConstraint(terrain);
		Assert.assertNotNull(terrainConstraint);

		final AccessConstraint weather = new AccessConstraint();
		weather.setName(ConstraintUtils.weatherConstraintName);
		weather.setMaxValue(0.3);
		final IAccessConstraint weatherConstraint = constraintUtils.buildConstraint(weather);
		Assert.assertNotNull(weatherConstraint);

		final List<IAccessConstraint> constraints = Arrays.asList(	qualityConstraint,
																	terrainConstraint,
																	weatherConstraint);

		final List<UntrimmedAccess> accesses = accessService.getAccesses(	Collections.singletonList(asset),
																			startTime,
																			endTime,
																			constraints,
																			SENSOR_TYPE,
																			geometry,
																			TARGET_TYPE,
																			PROPAGATOR_TYPE,
																			ORDER_OF_BATTLE);

		Assert.assertNotNull(accesses);

		Assert.assertFalse(accesses.isEmpty());
	}

	@Test
	public void testGetAccessesWithTerrainAndWeatherConstraintsCloudCover()
			throws ParseException,
			IOException {
		Mockito.when(restTemplate.getForEntity(	Mockito.any(URI.class),
		                                       	Mockito.eq(JsonNode.class)))
				.thenReturn(ResponseEntity.ok(tleJsonNodeResponse));

		final DateTime startTime = DateTime.parse(START_TIME);
		final DateTime endTime = DateTime.parse(END_TIME);

		final TimeInterval timeInterval = new TimeInterval();
		timeInterval.setStart(startTime);
		timeInterval.setEnd(endTime);

		final GoodTimeIntervalsResponse goodTimeIntervalsResponse = new GoodTimeIntervalsResponse();
		goodTimeIntervalsResponse.setTimeIntervals(Collections.singletonList(timeInterval));

		Mockito.when(restTemplate.postForEntity(ArgumentMatchers.eq(terrainUrl),
												ArgumentMatchers.any(),
												ArgumentMatchers.eq(GoodTimeIntervalsResponse.class)))
				.thenReturn(ResponseEntity.ok(goodTimeIntervalsResponse));

		Mockito.when(restTemplate.getForEntity(	ArgumentMatchers.any(URI.class),
												ArgumentMatchers.eq(Double.class)))
				.thenReturn(ResponseEntity.ok(8.0));

		final Geometry geometry = parseGeometry();

		final Asset asset = buildRS02();
		Assert.assertNotNull(asset);

		final AccessConstraint qual = new AccessConstraint();
		qual.setName(ConstraintUtils.qualityConstraintName);
		qual.setMinValue(4.0);
		final IAccessConstraint qualityConstraint = constraintUtils.buildConstraint(qual);
		Assert.assertNotNull(qualityConstraint);

		final AccessConstraint terrain = new AccessConstraint();
		terrain.setName(ConstraintUtils.terrainConstraintName);
		final IAccessConstraint terrainConstraint = constraintUtils.buildConstraint(terrain);
		Assert.assertNotNull(terrainConstraint);

		final AccessConstraint weather = new AccessConstraint();
		weather.setName(ConstraintUtils.weatherConstraintName);
		weather.setMaxValue(0.3);
		final IAccessConstraint weatherConstraint = constraintUtils.buildConstraint(weather);
		Assert.assertNotNull(weatherConstraint);

		final List<IAccessConstraint> constraints = Arrays.asList(	qualityConstraint,
																	terrainConstraint,
																	weatherConstraint);

		final List<UntrimmedAccess> accesses = accessService.getAccesses(	Collections.singletonList(asset),
																			startTime,
																			endTime,
																			constraints,
																			SENSOR_TYPE,
																			geometry,
																			TARGET_TYPE,
																			PROPAGATOR_TYPE,
																			ORDER_OF_BATTLE);

		Assert.assertNotNull(accesses);

		Assert.assertFalse(accesses.isEmpty());
	}

	@Test
	public void testGetDetailsAtTime()
			throws ParseException,
			IOException,
			NoEphemerisFoundException,
			PropagatorTypeDoesNotExistException {
		Mockito.when(restTemplate.getForEntity(	Mockito.any(URI.class),
		                                       	Mockito.eq(JsonNode.class)))
				.thenReturn(ResponseEntity.ok(tleJsonNodeResponse));

		final DateTime atTime = DateTime.parse(START_TIME);
		final Geometry geometry = parseGeometry();

		final Asset asset = buildRS02();
		Assert.assertNotNull(asset);

		final ISensorMode sensorMode = AssetUtils.getSensorModeByName(	asset,
																		SENSOR_MODE);

		final AccessValues accessValues = accessService.getDetailsAtTime(	geometry,
																			atTime,
																			asset,
																			sensorMode,
																			PROPAGATOR_TYPE);

		Assert.assertNotNull(accessValues);
		Assert.assertEquals(atTime.toString(),
							accessValues.getAtTimeISO8601());
	}

	private Asset buildRS02()
			throws IOException {
		final InputStream xmlStream = Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream("RS02.xml");

		if (xmlStream == null) {
			return null;
		}

		final String modelXml = IOUtils.toString(	xmlStream,
													(Charset) null);
		return assetUtils.buildAssetFromModel(	"RS02",
												modelXml,
												null);
	}

	private static Geometry parseGeometry()
			throws ParseException {
		final WKTReader reader = new WKTReader();

		return reader.read(GEOMETRY_WKT);
	}
}
