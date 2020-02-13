package com.maxar.workflow.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.access.model.AccessConstraint;
import com.maxar.access.model.AccessValues;
import com.maxar.access.model.UntrimmedAccess;
import com.maxar.target.model.OrderOfBattle;
import com.maxar.target.model.PointTargetModel;
import com.maxar.target.model.TargetModel;
import com.maxar.target.model.TargetType;
import com.maxar.workflow.model.Access;
import com.maxar.workflow.model.AccessDetailsRequest;
import com.maxar.workflow.model.AccessTarget;
import com.maxar.workflow.model.TargetAccessRequest;
import com.maxar.workflow.model.TargetGeometryAccessRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testworkflowms.properties")
public class WorkflowAccessServiceTest
{
	private static final String EXAMPLE_CONSTRAINT_NAME = "constraint";

	@Autowired
	private WorkflowAccessService workflowAccessService;

	@MockBean
	private ApiService apiService;

	private final GeometryFactory geometryFactory = new GeometryFactory();

	private final TargetModel targetModel = new PointTargetModel(
			TargetType.POINT,
			"0",
			"target0",
			"",
			"country0",
			"",
			false,
			null,
			null,
			OrderOfBattle.GROUND,
			new Point(
					new CoordinateArraySequence(
							Arrays.array(new CoordinateXY(
									0.0,
									0.0))),
					geometryFactory),
			0.0,
			0.0,
			0.0,
			0.0,
			0.0,
			0.0);

	private final UntrimmedAccess spaceUntrimmedAccess = new UntrimmedAccess();

	private final AccessValues spaceAccessValues = new AccessValues();

	@Before
	public void setUp() {
		spaceUntrimmedAccess.setAssetID("space0");
		spaceUntrimmedAccess.setAssetName("space0");
		spaceUntrimmedAccess.setStartTimeISO8601("2019-07-01T03:45:00");
		spaceUntrimmedAccess.setEndTimeISO8601("2019-07-01T04:15:00");
		spaceUntrimmedAccess.setTcaTimeISO8601("2019-07-01T04:00:00");

		spaceUntrimmedAccess.setSensorMode("sensor mode");
		spaceUntrimmedAccess.setSensorType("sensor type");
		spaceUntrimmedAccess.setPropagatorType("J2");
		spaceUntrimmedAccess.setCzml(Collections.emptyList());

		final com.maxar.access.model.Access spaceTrimmedAccess = new com.maxar.access.model.Access();
		spaceTrimmedAccess.setStartTimeISO8601("2019-07-01T03:55:00");
		spaceTrimmedAccess.setEndTimeISO8601("2019-07-01T03:59:00");
		spaceTrimmedAccess.setTcaTimeISO8601("2019-07-01T03:57:00");

		spaceUntrimmedAccess.setTrimmedAccesses(Collections.singletonList(spaceTrimmedAccess));

		Mockito.when(apiService.getSpaceAccessesByGeometryAndTimes(	ArgumentMatchers.anyString(),
																	ArgumentMatchers.anyString(),
																	ArgumentMatchers.anyString(),
																	ArgumentMatchers.anyList(),
																	ArgumentMatchers.anyList(),
																	ArgumentMatchers.isNull(),
																	ArgumentMatchers.isNull()))
				.thenReturn(Collections.singletonList(spaceUntrimmedAccess));

		spaceAccessValues.setAzimuthDeg(0.0);
		spaceAccessValues.setElevationDeg(0.0);
		spaceAccessValues.setQuality(0.0);

		Mockito.when(apiService.getSpaceAccessDetails(	ArgumentMatchers.anyString(),
														ArgumentMatchers.any(),
														ArgumentMatchers.anyString(),
														ArgumentMatchers.anyString(),
														ArgumentMatchers.anyString()))
				.thenReturn(spaceAccessValues);

		Mockito.when(apiService.getTargetById(ArgumentMatchers.anyString()))
				.thenReturn(Optional.of(targetModel));

		Mockito.when(apiService.generateWeatherByDateAndGeometryRequest(Mockito.anyString(),
																		Mockito.anyString()))
				.thenReturn(null);

		Mockito.when(apiService.getAccessWeather(Mockito.isNull()))
				.thenReturn(0.0);

		Mockito.when(apiService.getAccessConstraintNames())
				.thenReturn(Collections.singletonList(EXAMPLE_CONSTRAINT_NAME));
	}

	@Test
	public void testGetAccessConstraintNames() {
		final List<String> names = workflowAccessService.getAccessConstraintNames();

		Assert.assertNotNull(names);
		Assert.assertEquals(1,
							names.size());
		Assert.assertEquals(EXAMPLE_CONSTRAINT_NAME,
							names.get(0));
	}

	@Test
	public void testGetAccessesForTargetsAndAssets() {
		final TargetAccessRequest accessRequest = new TargetAccessRequest();
		accessRequest.setTargetIds(Collections.singletonList(targetModel.getTargetId()));
		accessRequest.setSpaceAssetIds(Collections.singletonList("space0"));
		accessRequest.setStart("2019-06-01T16:00:00");
		accessRequest.setStop("2019-06-03T19:25:00");
		final AccessConstraint accessConstraint = new AccessConstraint();
		accessRequest.setAccessConstraints(Collections.singletonList(accessConstraint));

		final List<Access> accesses = workflowAccessService.getAccessesForTargetsAndAssets(accessRequest);

		Assert.assertNotNull(accesses);
		Assert.assertFalse(accesses.isEmpty());
		Assert.assertEquals(targetModel.getTargetId(),
							accesses.get(0)
									.getTargetId());
		Assert.assertEquals(spaceUntrimmedAccess.getAssetID(),
							accesses.get(0)
									.getAssetId());

		Assert.assertEquals("Untrimmed",
							accesses.get(0)
									.getType());

		Assert.assertNotNull(accesses.get(0)
				.getStartDetails());

		Assert.assertNotNull(accesses.get(0)
				.getStopDetails());

		Assert.assertNotNull(accesses.get(0)
				.getTcaDetails());

		Assert.assertEquals("Trimmed",
							accesses.get(1)
									.getType());

		Assert.assertNotNull(accesses.get(1)
				.getStartDetails());

		Assert.assertNotNull(accesses.get(1)
				.getStopDetails());

		Assert.assertNotNull(accesses.get(1)
				.getTcaDetails());
	}

	@Test
	public void testGetAccessesForTargetsAndAssetsNoAssetsProvided() {

		final TargetAccessRequest accessRequest = new TargetAccessRequest();
		accessRequest.setTargetIds(Collections.singletonList(targetModel.getTargetId()));
		accessRequest.setSpaceAssetIds(Collections.emptyList());
		final AccessConstraint accessConstraint = new AccessConstraint();
		accessRequest.setAccessConstraints(Collections.singletonList(accessConstraint));

		final List<Access> accesses = workflowAccessService.getAccessesForTargetsAndAssets(accessRequest);

		Assert.assertNotNull(accesses);
		Assert.assertTrue(accesses.isEmpty());
	}

	@Test
	public void testGetAccessesForTargetsAndAssetsDoesNotExist() {
		final TargetAccessRequest accessRequest = new TargetAccessRequest();
		accessRequest.setTargetIds(Collections.singletonList(targetModel.getTargetId()));
		accessRequest.setSpaceAssetIds(Collections.singletonList("space0"));
		final AccessConstraint accessConstraint = new AccessConstraint();
		accessRequest.setAccessConstraints(Collections.singletonList(accessConstraint));

		final List<Access> accesses = workflowAccessService.getAccessesForTargetsAndAssets(accessRequest);

		Assert.assertNotNull(accesses);
		Assert.assertTrue(accesses.isEmpty());
	}

	@Test
	public void testGetAccessesForTargetGeometriesAndAssets() {
		final AccessTarget accessTarget = new AccessTarget();
		accessTarget.setTargetId(targetModel.getTargetId());
		accessTarget.setGeometry(targetModel.getGeometry()
				.toString());

		final TargetGeometryAccessRequest accessRequest = new TargetGeometryAccessRequest();
		accessRequest.setTargets(Collections.singletonList(accessTarget));
		accessRequest.setSpaceAssetIds(Collections.singletonList("space0"));
		accessRequest.setStart("2019-06-01T16:00:00");
		accessRequest.setStop("2019-06-03T19:25:00");
		final AccessConstraint accessConstraint = new AccessConstraint();
		accessRequest.setAccessConstraints(Collections.singletonList(accessConstraint));

		final List<Access> accesses = workflowAccessService.getAccessesForTargetGeometriesAndAssets(accessRequest);

		Assert.assertNotNull(accesses);
		Assert.assertFalse(accesses.isEmpty());
		Assert.assertEquals(targetModel.getTargetId(),
							accesses.get(0)
									.getTargetId());
		Assert.assertEquals(spaceUntrimmedAccess.getAssetID(),
							accesses.get(0)
									.getAssetId());
		Assert.assertNull(accesses.get(0)
				.getTargetName());
		Assert.assertNull(accesses.get(0)
				.getCountryCode());
		Assert.assertNull(accesses.get(0)
				.getGeoRegion());

		Assert.assertEquals("Untrimmed",
							accesses.get(0)
									.getType());

		Assert.assertNotNull(accesses.get(0)
				.getStartDetails());

		Assert.assertNotNull(accesses.get(0)
				.getStopDetails());

		Assert.assertNotNull(accesses.get(0)
				.getTcaDetails());

		Assert.assertEquals("Trimmed",
							accesses.get(1)
									.getType());

		Assert.assertNotNull(accesses.get(1)
				.getStartDetails());

		Assert.assertNotNull(accesses.get(1)
				.getStopDetails());

		Assert.assertNotNull(accesses.get(1)
				.getTcaDetails());
	}

	@Test
	public void testGetAccessesForTargetGeometriesAndAssetsExtraFields() {
		final AccessTarget accessTarget = new AccessTarget();
		accessTarget.setTargetId(targetModel.getTargetId());
		accessTarget.setGeometry(targetModel.getGeometry()
				.toString());
		accessTarget.setTargetName(targetModel.getTargetName());
		accessTarget.setCountryCode(targetModel.getCountryCode());
		accessTarget.setGeoRegion(targetModel.getGeoRegion());

		final TargetGeometryAccessRequest accessRequest = new TargetGeometryAccessRequest();
		accessRequest.setTargets(Collections.singletonList(accessTarget));
		accessRequest.setSpaceAssetIds(Collections.singletonList("space0"));
		accessRequest.setStart("2019-06-01T16:00:00");
		accessRequest.setStop("2019-06-03T19:25:00");
		final AccessConstraint accessConstraint = new AccessConstraint();
		accessRequest.setAccessConstraints(Collections.singletonList(accessConstraint));

		final List<Access> accesses = workflowAccessService.getAccessesForTargetGeometriesAndAssets(accessRequest);

		Assert.assertNotNull(accesses);
		Assert.assertFalse(accesses.isEmpty());
		Assert.assertEquals(targetModel.getTargetId(),
							accesses.get(0)
									.getTargetId());
		Assert.assertEquals(spaceUntrimmedAccess.getAssetID(),
							accesses.get(0)
									.getAssetId());
		Assert.assertEquals(targetModel.getTargetName(),
							accesses.get(0)
									.getTargetName());
		Assert.assertEquals(targetModel.getCountryCode(),
							accesses.get(0)
									.getCountryCode());
		Assert.assertEquals(targetModel.getGeoRegion(),
							accesses.get(0)
									.getGeoRegion());

		Assert.assertEquals("Untrimmed",
							accesses.get(0)
									.getType());

		Assert.assertNotNull(accesses.get(0)
				.getStartDetails());

		Assert.assertNotNull(accesses.get(0)
				.getStopDetails());

		Assert.assertNotNull(accesses.get(0)
				.getTcaDetails());

		Assert.assertEquals("Trimmed",
							accesses.get(1)
									.getType());

		Assert.assertNotNull(accesses.get(1)
				.getStartDetails());

		Assert.assertNotNull(accesses.get(1)
				.getStopDetails());

		Assert.assertNotNull(accesses.get(1)
				.getTcaDetails());
	}

	@Test
	public void testGetAccessesForTargetGeometriesAndAssetsNoAssetsProvided() {
		final AccessTarget accessTarget = new AccessTarget();
		accessTarget.setTargetId(targetModel.getTargetId());
		accessTarget.setGeometry(targetModel.getGeometry()
				.toString());

		final TargetGeometryAccessRequest accessRequest = new TargetGeometryAccessRequest();
		accessRequest.setTargets(Collections.singletonList(accessTarget));
		accessRequest.setSpaceAssetIds(Collections.emptyList());
		final AccessConstraint accessConstraint = new AccessConstraint();
		accessRequest.setAccessConstraints(Collections.singletonList(accessConstraint));

		final List<Access> accesses = workflowAccessService.getAccessesForTargetGeometriesAndAssets(accessRequest);

		Assert.assertNotNull(accesses);
		Assert.assertTrue(accesses.isEmpty());
	}

	@Test
	public void testGetAccessesForTargetGeometriesAndAssetsDoesNotExist() {
		final AccessTarget accessTarget = new AccessTarget();
		accessTarget.setTargetId(targetModel.getTargetId());
		accessTarget.setGeometry(targetModel.getGeometry()
				.toString());

		final TargetGeometryAccessRequest accessRequest = new TargetGeometryAccessRequest();
		accessRequest.setTargets(Collections.singletonList(accessTarget));
		accessRequest.setSpaceAssetIds(Collections.singletonList("space0"));
		final AccessConstraint accessConstraint = new AccessConstraint();
		accessRequest.setAccessConstraints(Collections.singletonList(accessConstraint));

		final List<Access> accesses = workflowAccessService.getAccessesForTargetGeometriesAndAssets(accessRequest);

		Assert.assertNotNull(accesses);
		Assert.assertTrue(accesses.isEmpty());
	}

	@Test
	public void testGetAccessDetails() {
		final AccessDetailsRequest accessDetailsRequest = new AccessDetailsRequest();
		Mockito.when(apiService.getAccessDetails(Mockito.eq(accessDetailsRequest)))
				.thenReturn(new AccessValues());

		final AccessValues response = workflowAccessService.getAccessDetails(accessDetailsRequest);

		Assert.assertNotNull(response);
	}
}