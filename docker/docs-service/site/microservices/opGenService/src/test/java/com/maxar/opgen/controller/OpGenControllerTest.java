package com.maxar.opgen.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.asset.common.client.airborne.AssetRetrieverAirborne;
import com.maxar.asset.common.client.space.AssetRetrieverSpace;
import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.exception.AssetNameDoesNotExistException;
import com.maxar.asset.common.exception.MissionIdDoesNotExistException;
import com.maxar.asset.common.exception.NoEphemerisFoundException;
import com.maxar.asset.common.exception.NoMissionsFoundException;
import com.maxar.asset.common.exception.PropagatorTypeDoesNotExistException;
import com.maxar.asset.common.exception.SensorModeNameDoesNotExistException;
import com.maxar.asset.common.service.ApiService;
import com.maxar.common.exception.BadRequestException;
import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.ephemeris.model.TLEModel;
import com.maxar.mission.model.MissionModel;
import com.maxar.mission.model.TrackModel;
import com.maxar.mission.model.TrackNodeModel;
import com.maxar.opgen.model.Op;
import com.maxar.opgen.model.OpAirborneRequest;
import com.maxar.opgen.model.OpSpaceRequest;
import com.radiantblue.analytics.isr.core.model.asset.Asset;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testopgen.properties")
public class OpGenControllerTest
{
	private static final String GOOD_SPACE_ASSET_ID = "CSM3";

	private static final String GOOD_PROPAGATOR_TYPE = "J2";

	private static final String GOOD_SPACE_SENSOR_MODE_NAME = "CSM3_Framing_Mode";

	private static final String BAD_SPACE_SENSOR_MODE_NAME = "BAD_Framing_Mode";

	private static final DateTime GOOD_SPACE_START_TIME = DateTime.parse("2020-01-01T03:00:00.000Z");

	private static final DateTime GOOD_SPACE_END_TIME = DateTime.parse("2020-01-01T03:05:00.000Z");

	private static final int GOOD_OP_SAMPLE_TIME_MS = 60000;

	private static final String GOOD_GEOMETRY_WKT = "POLYGON((117.23 33.94 0.0,117.25 33.94 0.0,117.25 33.92 0.0,117.23 33.92 0.0,117.23 33.94 0.0))";

	private static final String BAD_GEOMETRY_WKT = "P O L Y G O N((117.23 33.94 0.0,117.25 33.94 0.0,117.25 33.92 0.0,117.23 33.92 0.0,117.23 33.94 0.0))";

	private static final String GOOD_AIRBORNE_ASSET_ID = "AIR_SAR";

	private static final String GOOD_AIRBORNE_SENSOR_MODE = "AIR_SAR_Framing_Mode";

	private static final DateTime GOOD_AIRBORNE_START_TIME = DateTime.parse("2020-01-01T16:15:00Z");

	private static final DateTime GOOD_AIRBORNE_END_TIME = DateTime.parse("2020-01-01T16:20:00Z");

	private static final String GOOD_AIRBORNE_MISSION_ID = "AIRBORNE_MISSION";

	private static final GeometryFactory geomFactory = new GeometryFactory();

	@MockBean
	private ApiService apiService;

	@MockBean
	private AssetRetrieverSpace spaceClient;

	@MockBean
	private AssetRetrieverAirborne airborneClient;

	@Autowired
	private OpGenController opGenController;

	@Before
	public void setUp()
			throws Exception {
		final Asset GOOD_SPACE_ASSET = generateAsset(	"CSM3",
														"CSM3.xml");

		final TLEModel goodTLE = new TLEModel(
				33412,
				EphemerisType.TLE,
				1577781479725L,
				"COSMO-SKYMED 3",
				"1 33412U 08054A   19365.35971905  .00000321  00000-0  46905-4 0  9993",
				"2 33412  97.8914  84.4340 0001429  75.2834 284.8530 14.82152602589734");

		Mockito.when(spaceClient.getAssetModelByName(GOOD_SPACE_ASSET_ID))
				.thenReturn(GOOD_SPACE_ASSET);

		Mockito.when(apiService.getAssetEphemerisAtTime(	"33412",
													GOOD_SPACE_START_TIME))
				.thenReturn(goodTLE);

		final Asset GOOD_AIRBORNE_ASSET = generateAsset("AIR_SAR",
														"AIR_SAR.xml");

		final MissionModel GOOD_MISSION = generateTestMission();

		Mockito.when(airborneClient.getAssetModelByName(GOOD_AIRBORNE_ASSET_ID))
				.thenReturn(GOOD_AIRBORNE_ASSET);

		Mockito.when(apiService.getAssetMissionsAtTime(	GOOD_AIRBORNE_ASSET_ID,
														GOOD_AIRBORNE_START_TIME,
														GOOD_AIRBORNE_END_TIME))
				.thenReturn(Collections.singletonList(GOOD_MISSION));

		Mockito.when(apiService.getAssetMissionsAtTime(	GOOD_AIRBORNE_ASSET_ID,
														GOOD_AIRBORNE_START_TIME,
														GOOD_AIRBORNE_START_TIME))
				.thenReturn(Collections.singletonList(GOOD_MISSION));
	}

	@Test
	public void testCreateOpSpace()
			throws AssetNameDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			PropagatorTypeDoesNotExistException,
			SensorModeNameDoesNotExistException,
			NoEphemerisFoundException {
		final OpSpaceRequest request = new OpSpaceRequest();
		request.setAssetName(GOOD_SPACE_ASSET_ID);
		request.setSensorModeName(GOOD_SPACE_SENSOR_MODE_NAME);
		request.setPropagatorType(GOOD_PROPAGATOR_TYPE);
		request.setStartTime(GOOD_SPACE_START_TIME);
		request.setEndTime(GOOD_SPACE_END_TIME);
		request.setOpSampleTime_ms(GOOD_OP_SAMPLE_TIME_MS);
		request.setTargetGeometryWkt(GOOD_GEOMETRY_WKT);

		final ResponseEntity<List<Op>> response = opGenController.createOpSpace(request);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());

		final List<Op> ops = response.getBody();

		Assert.assertNotNull(ops);
		Assert.assertEquals(6,
							ops.size());
	}

	@Test
	public void testCreateOpSpaceZeroSampleMs()
			throws AssetNameDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			PropagatorTypeDoesNotExistException,
			SensorModeNameDoesNotExistException,
			NoEphemerisFoundException {
		final OpSpaceRequest request = new OpSpaceRequest();
		request.setAssetName(GOOD_SPACE_ASSET_ID);
		request.setSensorModeName(GOOD_SPACE_SENSOR_MODE_NAME);
		request.setPropagatorType(GOOD_PROPAGATOR_TYPE);
		request.setStartTime(GOOD_SPACE_START_TIME);
		request.setEndTime(GOOD_SPACE_END_TIME);
		request.setOpSampleTime_ms(0);
		request.setTargetGeometryWkt(GOOD_GEOMETRY_WKT);

		final ResponseEntity<List<Op>> response = opGenController.createOpSpace(request);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());

		final List<Op> ops = response.getBody();

		Assert.assertNotNull(ops);
		Assert.assertEquals(301,
							ops.size());
	}

	@Test(expected = SensorModeNameDoesNotExistException.class)
	public void testCreateOpSpaceBadSensorMode()
			throws AssetNameDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			PropagatorTypeDoesNotExistException,
			SensorModeNameDoesNotExistException,
			NoEphemerisFoundException {
		final OpSpaceRequest request = new OpSpaceRequest();
		request.setAssetName(GOOD_SPACE_ASSET_ID);
		request.setSensorModeName(BAD_SPACE_SENSOR_MODE_NAME);
		request.setPropagatorType(GOOD_PROPAGATOR_TYPE);
		request.setStartTime(GOOD_SPACE_START_TIME);
		request.setEndTime(GOOD_SPACE_END_TIME);
		request.setOpSampleTime_ms(GOOD_OP_SAMPLE_TIME_MS);
		request.setTargetGeometryWkt(GOOD_GEOMETRY_WKT);

		final ResponseEntity<List<Op>> response = opGenController.createOpSpace(request);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());

		final List<Op> ops = response.getBody();

		Assert.assertNotNull(ops);
		Assert.assertEquals(6,
							ops.size());
	}

	@Test(expected = BadRequestException.class)
	public void testCreateOpSpaceBadGeometry()
			throws AssetNameDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			PropagatorTypeDoesNotExistException,
			SensorModeNameDoesNotExistException,
			NoEphemerisFoundException {
		final OpSpaceRequest request = new OpSpaceRequest();
		request.setAssetName(GOOD_SPACE_ASSET_ID);
		request.setSensorModeName(GOOD_SPACE_SENSOR_MODE_NAME);
		request.setPropagatorType(GOOD_PROPAGATOR_TYPE);
		request.setStartTime(GOOD_SPACE_START_TIME);
		request.setEndTime(GOOD_SPACE_END_TIME);
		request.setOpSampleTime_ms(GOOD_OP_SAMPLE_TIME_MS);
		request.setTargetGeometryWkt(BAD_GEOMETRY_WKT);

		opGenController.createOpSpace(request);
	}

	@Test
	public void testCreateOpAirborne()
			throws AssetNameDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			SensorModeNameDoesNotExistException,
			NoMissionsFoundException,
			MissionIdDoesNotExistException {
		final OpAirborneRequest request = new OpAirborneRequest();
		request.setAssetName(GOOD_AIRBORNE_ASSET_ID);
		request.setSensorModeName(GOOD_AIRBORNE_SENSOR_MODE);
		request.setStartTime(GOOD_AIRBORNE_START_TIME);
		request.setEndTime(GOOD_AIRBORNE_END_TIME);
		request.setOpSampleTime_ms(GOOD_OP_SAMPLE_TIME_MS);
		request.setTargetGeometryWkt(GOOD_GEOMETRY_WKT);
		request.setMissionId(GOOD_AIRBORNE_MISSION_ID);

		final ResponseEntity<List<Op>> response = opGenController.createOpAirborne(request);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());

		final List<Op> ops = response.getBody();

		Assert.assertNotNull(ops);
		Assert.assertEquals(6,
							ops.size());
	}

	@Test
	public void testCreateOpAirborneNoEndTime()
			throws AssetNameDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			SensorModeNameDoesNotExistException,
			NoMissionsFoundException,
			MissionIdDoesNotExistException {
		final OpAirborneRequest request = new OpAirborneRequest();
		request.setAssetName(GOOD_AIRBORNE_ASSET_ID);
		request.setSensorModeName(GOOD_AIRBORNE_SENSOR_MODE);
		request.setStartTime(GOOD_AIRBORNE_START_TIME);
		request.setOpSampleTime_ms(GOOD_OP_SAMPLE_TIME_MS);
		request.setTargetGeometryWkt(GOOD_GEOMETRY_WKT);
		request.setMissionId(GOOD_AIRBORNE_MISSION_ID);

		final ResponseEntity<List<Op>> response = opGenController.createOpAirborne(request);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());

		final List<Op> ops = response.getBody();

		Assert.assertNotNull(ops);
		Assert.assertEquals(1,
							ops.size());
	}

	private static Asset generateAsset(
			final String assetName,
			final String xmlFile )
			throws IOException {

		final InputStream xmlStream = Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(xmlFile);
		final String modelXml = IOUtils.toString(	xmlStream,
													(Charset) null);

		final GenericXmlApplicationContext appContext = new GenericXmlApplicationContext();
		final Resource assetXmlResource = new ByteArrayResource(
				modelXml.getBytes());
		appContext.load(assetXmlResource);

		appContext.refresh();

		final Asset asset = (Asset) appContext.getBean(assetName);
		asset.init();
		appContext.close();

		return asset;
	}

	private MissionModel generateTestMission() {
		// Create Track Nodes
		final List<TrackNodeModel> trackNodes = new ArrayList<>();
		final List<Coordinate> trackCoords = new ArrayList<>();
		Coordinate coord = new Coordinate(
				118.195678782655,
				34.4755069231593);
		trackCoords.add(coord);
		trackNodes.add(new TrackNodeModel(
				1,
				convertCoordToPoint(coord),
				0l,
				true));
		coord = new Coordinate(
				118.920619027317,
				34.8878933792785);
		trackCoords.add(coord);
		trackNodes.add(new TrackNodeModel(
				2,
				convertCoordToPoint(coord),
				449999l,
				true));
		coord = new Coordinate(
				119.424094768776,
				34.2918542225486);
		trackCoords.add(coord);
		trackNodes.add(new TrackNodeModel(
				3,
				convertCoordToPoint(coord),
				899998l,
				true));
		coord = new Coordinate(
				120.28449238522,
				34.4321944338458);
		trackCoords.add(coord);
		trackNodes.add(new TrackNodeModel(
				4,
				convertCoordToPoint(coord),
				1349997l,
				true));

		// Create Track
		final CoordinateArraySequence trackCoordArray = new CoordinateArraySequence(
				trackCoords.stream()
						.toArray(Coordinate[]::new));

		final Geometry trackLine = new LineString(
				trackCoordArray,
				geomFactory);

		final TrackModel track = new TrackModel(
				"TESTTRACK",
				"TESTTRACK",
				trackNodes,
				trackLine);

		// Create Mission
		final MissionModel mission = new MissionModel(
				GOOD_AIRBORNE_MISSION_ID,
				GOOD_AIRBORNE_MISSION_ID,
				GOOD_AIRBORNE_ASSET_ID,
				track,
				1577894930588l,
				1577896280585l,
				400.0,
				13157.14854759);

		return mission;
	}

	private Point convertCoordToPoint(
			final Coordinate coord ) {

		final Coordinate[] coordArray = {
			coord
		};

		final CoordinateArraySequence cas = new CoordinateArraySequence(
				coordArray);

		final Point point = new Point(
				cas,
				geomFactory);
		return point;
	}
}
