package com.maxar.airborne.access.controller;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.access.common.exception.NoMatchingAssetsException;
import com.maxar.access.common.exception.UnsupportedSensorTypeException;
import com.maxar.access.common.service.AccessService;
import com.maxar.access.common.service.SupportingServiceClient;
import com.maxar.access.model.AccessConstraint;
import com.maxar.access.model.AccessValues;
import com.maxar.access.model.AirborneAccessGenerationRequest;
import com.maxar.access.model.UntrimmedAccess;
import com.maxar.asset.common.client.airborne.AssetRetrieverAirborne;
import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.exception.AssetNameDoesNotExistException;
import com.maxar.asset.common.exception.MissionIdDoesNotExistException;
import com.maxar.asset.common.exception.NoMissionsFoundException;
import com.maxar.common.exception.BadRequestException;
import com.maxar.target.model.OrderOfBattle;
import com.maxar.target.model.TargetModel;
import com.maxar.target.model.TargetType;
import com.radiantblue.analytics.isr.core.model.asset.Aircraft;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.isr.core.model.payload.Payload;
import com.radiantblue.analytics.isr.eo.EOSensor;
import com.radiantblue.analytics.isr.eo.scanner.EOScanningMode;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:airborneaccessms.properties")
public class AirborneAccessControllerTest
{
	private static final String EXAMPLE_ASSET_ID = "AIRBORNE0";

	private static final String EXAMPLE_START_TIME = "2019-01-01T00:00:00Z";

	private static final String EXAMPLE_END_TIME = "2019-01-02T00:00:00Z";

	private static final String EXAMPLE_TARGET_ID = "target0";

	private static final String EXAMPLE_WKT = "POINT(0.0 0.0)";

	private static final String SENSOR_TYPE_EO = "EO";

	private static final String SENSOR_MODE_EO = "EO_SENSOR";

	private static final String ACCESS_CONSTRAINT_TYPE_WEATHER = "Weather";

	private static final double ACCESS_CONSTRAINT_MAX_WEATHER = 0.2;

	@Autowired
	private AirborneAccessController controller;

	@MockBean
	private AccessService accessService;

	@MockBean
	private SupportingServiceClient serviceClient;

	@MockBean
	private AssetRetrieverAirborne assetClient;

	@Test
	public void testGetAccessesWithTarget()
			throws ParseException,
			AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			UnsupportedSensorTypeException,
			NoMatchingAssetsException {
		final WKTReader wktReader = new WKTReader();
		final Geometry geometry = wktReader.read(EXAMPLE_WKT);

		Mockito.when(serviceClient.lookupTarget(ArgumentMatchers.eq(EXAMPLE_TARGET_ID)))
				.thenReturn(TargetModel.builder()
						.targetId(EXAMPLE_TARGET_ID)
						.geometry(geometry)
						.targetType(TargetType.POINT)
						.orderOfBattle(OrderOfBattle.GROUND)
						.build());

		final EOSensor eoSensor = new EOSensor();

		final Payload payload = new Payload();
		payload.setSensors(Collections.singletonList(eoSensor));

		final Asset asset = new Aircraft();
		asset.setId(0);
		asset.setName(EXAMPLE_ASSET_ID);
		asset.setPayloads(Collections.singletonList(payload));

		Mockito.when(assetClient.getAssetModelByName(ArgumentMatchers.eq(EXAMPLE_ASSET_ID)))
				.thenReturn(asset);

		final UntrimmedAccess access = new UntrimmedAccess();

		Mockito.when(accessService.getAirborneAccesses(	ArgumentMatchers.anyList(),
														ArgumentMatchers.isNull(),
														ArgumentMatchers.any(),
														ArgumentMatchers.any(),
														ArgumentMatchers.anyList(),
														ArgumentMatchers.eq(SENSOR_TYPE_EO),
														ArgumentMatchers.any(),
														ArgumentMatchers.eq(TargetType.POINT),
														ArgumentMatchers.eq(OrderOfBattle.GROUND)))
				.thenReturn(Collections.singletonList(access));

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final AirborneAccessGenerationRequest request = new AirborneAccessGenerationRequest();
		request.setAssetNames(Collections.singletonList(EXAMPLE_ASSET_ID));
		request.setAccessConstraints(Collections.singletonList(accessConstraint));
		request.setStartTimeISO8601(EXAMPLE_START_TIME);
		request.setEndTimeISO8601(EXAMPLE_END_TIME);
		request.setTgtOrGeometryWkt(EXAMPLE_TARGET_ID);
		request.setSensorType(SENSOR_TYPE_EO);

		final ResponseEntity<List<UntrimmedAccess>> response = controller.getAccesses(request);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
	}

	@Test
	public void testGetAccessesWithWkt()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			UnsupportedSensorTypeException,
			NoMatchingAssetsException {
		final EOSensor eoSensor = new EOSensor();

		final Payload payload = new Payload();
		payload.setSensors(Collections.singletonList(eoSensor));

		final Asset asset = new Aircraft();
		asset.setId(0);
		asset.setName(EXAMPLE_ASSET_ID);
		asset.setPayloads(Collections.singletonList(payload));

		Mockito.when(assetClient.getAssetModelByName(ArgumentMatchers.eq(EXAMPLE_ASSET_ID)))
				.thenReturn(asset);

		final UntrimmedAccess access = new UntrimmedAccess();

		Mockito.when(accessService.getAirborneAccesses(	ArgumentMatchers.anyList(),
														ArgumentMatchers.isNull(),
														ArgumentMatchers.any(),
														ArgumentMatchers.any(),
														ArgumentMatchers.anyList(),
														ArgumentMatchers.eq(SENSOR_TYPE_EO),
														ArgumentMatchers.any(),
														ArgumentMatchers.eq(TargetType.POINT),
														ArgumentMatchers.eq(OrderOfBattle.GROUND)))
				.thenReturn(Collections.singletonList(access));

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final AirborneAccessGenerationRequest request = new AirborneAccessGenerationRequest();
		request.setAssetNames(Collections.singletonList(EXAMPLE_ASSET_ID));
		request.setAccessConstraints(Collections.singletonList(accessConstraint));
		request.setStartTimeISO8601(EXAMPLE_START_TIME);
		request.setEndTimeISO8601(EXAMPLE_END_TIME);
		request.setTgtOrGeometryWkt(EXAMPLE_WKT);
		request.setSensorType(SENSOR_TYPE_EO);

		final ResponseEntity<List<UntrimmedAccess>> response = controller.getAccesses(request);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
	}

	@Test
	public void testGetAccessesWithWktAssetsNull()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			UnsupportedSensorTypeException,
			NoMatchingAssetsException {
		Mockito.when(serviceClient.getAllAssetNames())
				.thenReturn(Collections.singletonList(EXAMPLE_ASSET_ID));

		final EOSensor eoSensor = new EOSensor();

		final Payload payload = new Payload();
		payload.setSensors(Collections.singletonList(eoSensor));

		final Asset asset = new Aircraft();
		asset.setId(0);
		asset.setName(EXAMPLE_ASSET_ID);
		asset.setPayloads(Collections.singletonList(payload));

		Mockito.when(assetClient.getAssetModelByName(ArgumentMatchers.eq(EXAMPLE_ASSET_ID)))
				.thenReturn(asset);

		final UntrimmedAccess access = new UntrimmedAccess();

		Mockito.when(accessService.getAirborneAccesses(	ArgumentMatchers.anyList(),
														ArgumentMatchers.isNull(),
														ArgumentMatchers.any(),
														ArgumentMatchers.any(),
														ArgumentMatchers.anyList(),
														ArgumentMatchers.eq(SENSOR_TYPE_EO),
														ArgumentMatchers.any(),
														ArgumentMatchers.eq(TargetType.POINT),
														ArgumentMatchers.eq(OrderOfBattle.GROUND)))
				.thenReturn(Collections.singletonList(access));

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final AirborneAccessGenerationRequest request = new AirborneAccessGenerationRequest();
		request.setAssetNames(null);
		request.setAccessConstraints(Collections.singletonList(accessConstraint));
		request.setStartTimeISO8601(EXAMPLE_START_TIME);
		request.setEndTimeISO8601(EXAMPLE_END_TIME);
		request.setTgtOrGeometryWkt(EXAMPLE_WKT);
		request.setSensorType(SENSOR_TYPE_EO);

		final ResponseEntity<List<UntrimmedAccess>> response = controller.getAccesses(request);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
	}

	@Test
	public void testGetAccessesWithWktAssetsEmpty()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			UnsupportedSensorTypeException,
			NoMatchingAssetsException {
		Mockito.when(serviceClient.getAllAssetNames())
				.thenReturn(Collections.singletonList(EXAMPLE_ASSET_ID));

		final EOSensor eoSensor = new EOSensor();

		final Payload payload = new Payload();
		payload.setSensors(Collections.singletonList(eoSensor));

		final Asset asset = new Aircraft();
		asset.setId(0);
		asset.setName(EXAMPLE_ASSET_ID);
		asset.setPayloads(Collections.singletonList(payload));

		Mockito.when(assetClient.getAssetModelByName(ArgumentMatchers.eq(EXAMPLE_ASSET_ID)))
				.thenReturn(asset);

		final UntrimmedAccess access = new UntrimmedAccess();

		Mockito.when(accessService.getAirborneAccesses(	ArgumentMatchers.anyList(),
														ArgumentMatchers.isNull(),
														ArgumentMatchers.any(),
														ArgumentMatchers.any(),
														ArgumentMatchers.anyList(),
														ArgumentMatchers.eq(SENSOR_TYPE_EO),
														ArgumentMatchers.any(),
														ArgumentMatchers.eq(TargetType.POINT),
														ArgumentMatchers.eq(OrderOfBattle.GROUND)))
				.thenReturn(Collections.singletonList(access));

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final AirborneAccessGenerationRequest request = new AirborneAccessGenerationRequest();
		request.setAssetNames(Collections.emptyList());
		request.setAccessConstraints(Collections.singletonList(accessConstraint));
		request.setStartTimeISO8601(EXAMPLE_START_TIME);
		request.setEndTimeISO8601(EXAMPLE_END_TIME);
		request.setTgtOrGeometryWkt(EXAMPLE_WKT);
		request.setSensorType(SENSOR_TYPE_EO);

		final ResponseEntity<List<UntrimmedAccess>> response = controller.getAccesses(request);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
	}

	@Test(expected = BadRequestException.class)
	public void testGetAccessesWithTargetNotFound()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			UnsupportedSensorTypeException,
			NoMatchingAssetsException {
		Mockito.when(serviceClient.lookupTarget(ArgumentMatchers.eq(EXAMPLE_TARGET_ID)))
				.thenReturn(null);

		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final AirborneAccessGenerationRequest request = new AirborneAccessGenerationRequest();
		request.setAssetNames(Collections.singletonList(EXAMPLE_ASSET_ID));
		request.setAccessConstraints(Collections.singletonList(accessConstraint));
		request.setStartTimeISO8601(EXAMPLE_START_TIME);
		request.setEndTimeISO8601(EXAMPLE_END_TIME);
		request.setTgtOrGeometryWkt(EXAMPLE_TARGET_ID);
		request.setSensorType(SENSOR_TYPE_EO);

		controller.getAccesses(request);
	}

	@Test(expected = BadRequestException.class)
	public void testGetAccessesWithWktStartTimeInvalid()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			UnsupportedSensorTypeException,
			NoMatchingAssetsException {
		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final AirborneAccessGenerationRequest request = new AirborneAccessGenerationRequest();
		request.setAssetNames(Collections.singletonList(EXAMPLE_ASSET_ID));
		request.setAccessConstraints(Collections.singletonList(accessConstraint));
		request.setStartTimeISO8601("invalid");
		request.setEndTimeISO8601(EXAMPLE_END_TIME);
		request.setTgtOrGeometryWkt(EXAMPLE_WKT);
		request.setSensorType(SENSOR_TYPE_EO);

		controller.getAccesses(request);
	}

	@Test(expected = BadRequestException.class)
	public void testGetAccessesWithWktEndTimeInvalid()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			UnsupportedSensorTypeException,
			NoMatchingAssetsException {
		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		final AirborneAccessGenerationRequest request = new AirborneAccessGenerationRequest();
		request.setAssetNames(Collections.singletonList(EXAMPLE_ASSET_ID));
		request.setAccessConstraints(Collections.singletonList(accessConstraint));
		request.setStartTimeISO8601(EXAMPLE_START_TIME);
		request.setEndTimeISO8601("invalid");
		request.setTgtOrGeometryWkt(EXAMPLE_WKT);
		request.setSensorType(SENSOR_TYPE_EO);

		controller.getAccesses(request);
	}

	@Test(expected = BadRequestException.class)
	public void testGetAccessesWithWktAssetNotFound()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			UnsupportedSensorTypeException,
			NoMatchingAssetsException {
		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName(ACCESS_CONSTRAINT_TYPE_WEATHER);
		accessConstraint.setMaxValue(ACCESS_CONSTRAINT_MAX_WEATHER);

		Mockito.when(assetClient.getAssetModelByName(ArgumentMatchers.eq(EXAMPLE_ASSET_ID)))
				.thenReturn(null);

		final AirborneAccessGenerationRequest request = new AirborneAccessGenerationRequest();
		request.setAssetNames(Collections.singletonList(EXAMPLE_ASSET_ID));
		request.setAccessConstraints(Collections.singletonList(accessConstraint));
		request.setStartTimeISO8601(EXAMPLE_START_TIME);
		request.setEndTimeISO8601(EXAMPLE_END_TIME);
		request.setTgtOrGeometryWkt(EXAMPLE_WKT);
		request.setSensorType(SENSOR_TYPE_EO);

		controller.getAccesses(request);
	}

	@Test(expected = BadRequestException.class)
	public void testGetAccessesWithWktConstraintInvalid()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			UnsupportedSensorTypeException,
			NoMatchingAssetsException {
		final AccessConstraint accessConstraint = new AccessConstraint();
		accessConstraint.setName("invalid");

		final EOSensor eoSensor = new EOSensor();

		final Payload payload = new Payload();
		payload.setSensors(Collections.singletonList(eoSensor));

		final Asset asset = new Aircraft();
		asset.setId(0);
		asset.setName(EXAMPLE_ASSET_ID);
		asset.setPayloads(Collections.singletonList(payload));

		Mockito.when(assetClient.getAssetModelByName(ArgumentMatchers.eq(EXAMPLE_ASSET_ID)))
				.thenReturn(asset);

		final AirborneAccessGenerationRequest request = new AirborneAccessGenerationRequest();
		request.setAssetNames(Collections.singletonList(EXAMPLE_ASSET_ID));
		request.setAccessConstraints(Collections.singletonList(accessConstraint));
		request.setStartTimeISO8601(EXAMPLE_START_TIME);
		request.setEndTimeISO8601(EXAMPLE_END_TIME);
		request.setTgtOrGeometryWkt(EXAMPLE_WKT);
		request.setSensorType(SENSOR_TYPE_EO);

		controller.getAccesses(request);
	}

	@Test
	public void testGetDetailsAtTimeWithTarget()
			throws ParseException,
			AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			NoMissionsFoundException,
			MissionIdDoesNotExistException {
		final WKTReader wktReader = new WKTReader();
		final Geometry geometry = wktReader.read(EXAMPLE_WKT);

		Mockito.when(serviceClient.lookupTarget(ArgumentMatchers.eq(EXAMPLE_TARGET_ID)))
				.thenReturn(TargetModel.builder()
						.targetId(EXAMPLE_TARGET_ID)
						.geometry(geometry)
						.targetType(TargetType.POINT)
						.orderOfBattle(OrderOfBattle.GROUND)
						.build());

		final EOScanningMode eoScanningMode = new EOScanningMode();
		eoScanningMode.setName(SENSOR_MODE_EO);

		final EOSensor eoSensor = new EOSensor();
		eoSensor.setModes(Collections.singletonList(eoScanningMode));

		final Payload payload = new Payload();
		payload.setSensors(Collections.singletonList(eoSensor));

		final Asset asset = new Aircraft();
		asset.setId(0);
		asset.setName(EXAMPLE_ASSET_ID);
		asset.setPayloads(Collections.singletonList(payload));

		Mockito.when(assetClient.getAssetModelByName(ArgumentMatchers.eq(EXAMPLE_ASSET_ID)))
				.thenReturn(asset);

		final AccessValues accessValues = new AccessValues();

		Mockito.when(accessService.getAirborneDetailsAtTime(ArgumentMatchers.any(),
															ArgumentMatchers.any(),
															ArgumentMatchers.any(),
															ArgumentMatchers.any()))
				.thenReturn(accessValues);

		final ResponseEntity<AccessValues> response = controller.getDetailsAtTime(	EXAMPLE_ASSET_ID,
																					EXAMPLE_TARGET_ID,
																					EXAMPLE_START_TIME,
																					SENSOR_MODE_EO);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testGetDetailsAtTimeWithWkt()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			NoMissionsFoundException,
			MissionIdDoesNotExistException {
		final EOScanningMode eoScanningMode = new EOScanningMode();
		eoScanningMode.setName(SENSOR_MODE_EO);

		final EOSensor eoSensor = new EOSensor();
		eoSensor.setModes(Collections.singletonList(eoScanningMode));

		final Payload payload = new Payload();
		payload.setSensors(Collections.singletonList(eoSensor));

		final Asset asset = new Aircraft();
		asset.setId(0);
		asset.setName(EXAMPLE_ASSET_ID);
		asset.setPayloads(Collections.singletonList(payload));

		Mockito.when(assetClient.getAssetModelByName(ArgumentMatchers.eq(EXAMPLE_ASSET_ID)))
				.thenReturn(asset);

		final AccessValues accessValues = new AccessValues();

		Mockito.when(accessService.getAirborneDetailsAtTime(ArgumentMatchers.any(),
															ArgumentMatchers.any(),
															ArgumentMatchers.any(),
															ArgumentMatchers.any()))
				.thenReturn(accessValues);

		final ResponseEntity<AccessValues> response = controller.getDetailsAtTime(	EXAMPLE_ASSET_ID,
																					EXAMPLE_WKT,
																					EXAMPLE_START_TIME,
																					SENSOR_MODE_EO);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test(expected = BadRequestException.class)
	public void testGetDetailsAtTimeWithTargetNotFound()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			NoMissionsFoundException,
			MissionIdDoesNotExistException {
		Mockito.when(serviceClient.lookupTarget(ArgumentMatchers.eq(EXAMPLE_TARGET_ID)))
				.thenReturn(null);

		final EOScanningMode eoScanningMode = new EOScanningMode();
		eoScanningMode.setName(SENSOR_MODE_EO);

		final EOSensor eoSensor = new EOSensor();
		eoSensor.setModes(Collections.singletonList(eoScanningMode));

		final Payload payload = new Payload();
		payload.setSensors(Collections.singletonList(eoSensor));

		final Asset asset = new Aircraft();
		asset.setId(0);
		asset.setName(EXAMPLE_ASSET_ID);
		asset.setPayloads(Collections.singletonList(payload));

		Mockito.when(assetClient.getAssetModelByName(ArgumentMatchers.eq(EXAMPLE_ASSET_ID)))
				.thenReturn(asset);

		final AccessValues accessValues = new AccessValues();

		Mockito.when(accessService.getAirborneDetailsAtTime(ArgumentMatchers.any(),
															ArgumentMatchers.any(),
															ArgumentMatchers.any(),
															ArgumentMatchers.any()))
				.thenReturn(accessValues);

		controller.getDetailsAtTime(EXAMPLE_ASSET_ID,
									EXAMPLE_TARGET_ID,
									EXAMPLE_START_TIME,
									SENSOR_MODE_EO);
	}

	@Test(expected = BadRequestException.class)
	public void testGetDetailsAtTimeWithWktAtTimeInvalid()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			NoMissionsFoundException,
			MissionIdDoesNotExistException {
		final EOScanningMode eoScanningMode = new EOScanningMode();
		eoScanningMode.setName(SENSOR_MODE_EO);

		final EOSensor eoSensor = new EOSensor();
		eoSensor.setModes(Collections.singletonList(eoScanningMode));

		final Payload payload = new Payload();
		payload.setSensors(Collections.singletonList(eoSensor));

		final Asset asset = new Aircraft();
		asset.setId(0);
		asset.setName(EXAMPLE_ASSET_ID);
		asset.setPayloads(Collections.singletonList(payload));

		Mockito.when(assetClient.getAssetModelByName(ArgumentMatchers.eq(EXAMPLE_ASSET_ID)))
				.thenReturn(asset);

		final AccessValues accessValues = new AccessValues();

		Mockito.when(accessService.getAirborneDetailsAtTime(ArgumentMatchers.any(),
															ArgumentMatchers.any(),
															ArgumentMatchers.any(),
															ArgumentMatchers.any()))
				.thenReturn(accessValues);

		controller.getDetailsAtTime(EXAMPLE_ASSET_ID,
									EXAMPLE_WKT,
									"invalid",
									SENSOR_MODE_EO);
	}

	@Test(expected = BadRequestException.class)
	public void testGetDetailsAtTimeWithWktAssetNotFound()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			NoMissionsFoundException,
			MissionIdDoesNotExistException {
		Mockito.when(assetClient.getAssetModelByName(ArgumentMatchers.eq(EXAMPLE_ASSET_ID)))
				.thenReturn(null);

		final AccessValues accessValues = new AccessValues();

		Mockito.when(accessService.getAirborneDetailsAtTime(ArgumentMatchers.any(),
															ArgumentMatchers.any(),
															ArgumentMatchers.any(),
															ArgumentMatchers.any()))
				.thenReturn(accessValues);

		controller.getDetailsAtTime(EXAMPLE_ASSET_ID,
									EXAMPLE_WKT,
									EXAMPLE_START_TIME,
									SENSOR_MODE_EO);
	}

	@Test(expected = BadRequestException.class)
	public void testGetDetailsAtTimeWithWktSensorModeNotFound()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetNameDoesNotExistException,
			NoMissionsFoundException,
			MissionIdDoesNotExistException {
		final EOScanningMode eoScanningMode = new EOScanningMode();
		eoScanningMode.setName(SENSOR_MODE_EO);

		final EOSensor eoSensor = new EOSensor();
		eoSensor.setModes(Collections.singletonList(eoScanningMode));

		final Payload payload = new Payload();
		payload.setSensors(Collections.singletonList(eoSensor));

		final Asset asset = new Aircraft();
		asset.setId(0);
		asset.setName(EXAMPLE_ASSET_ID);
		asset.setPayloads(Collections.singletonList(payload));

		Mockito.when(assetClient.getAssetModelByName(ArgumentMatchers.eq(EXAMPLE_ASSET_ID)))
				.thenReturn(asset);

		final AccessValues accessValues = new AccessValues();

		Mockito.when(accessService.getAirborneDetailsAtTime(ArgumentMatchers.any(),
															ArgumentMatchers.any(),
															ArgumentMatchers.any(),
															ArgumentMatchers.any()))
				.thenReturn(accessValues);

		controller.getDetailsAtTime(EXAMPLE_ASSET_ID,
									EXAMPLE_WKT,
									EXAMPLE_START_TIME,
									"unknown");
	}

	@Test
	public void testGetConstraintNames() {
		final ResponseEntity<List<String>> response = controller.getConstraintNames();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertFalse(response.getBody()
				.isEmpty());
		Assert.assertTrue(response.getBody()
				.contains(ACCESS_CONSTRAINT_TYPE_WEATHER));
	}
}
