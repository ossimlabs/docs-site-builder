package com.maxar.terrain.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.common.exception.BadRequestException;
import com.maxar.terrain.service.TerrainService;
import com.maxar.terrain.model.GoodTimeIntervalsRequest;
import com.maxar.terrain.model.GoodTimeIntervalsResponse;
import com.maxar.terrain.model.StateVector;
import com.maxar.terrain.model.TimeInterval;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test_terrainms.properties")
public class TerrainControllerTest
{
	@Autowired
	TerrainController terrainController;

	@MockBean
	TerrainService terrainService;

	@Before
	public void setUp() {
		Mockito.when(terrainService.getGoodTimeIntervals(	ArgumentMatchers.any(Geometry.class),
															ArgumentMatchers.anyList()))
				.thenReturn(Collections.singletonList(new Interval(
						DateTime.parse("2019-05-21T18:45:00.000Z"),
						DateTime.parse("2019-05-21T18:45:10.000Z"))));
	}

	@Test
	public void testGetGoodTimeIntervals() {
		final DateTime startTime = DateTime.parse("2019-05-21T18:45:00.000Z");
		final DateTime endTime = DateTime.parse("2019-05-21T18:45:10.000Z");

		GoodTimeIntervalsRequest request = new GoodTimeIntervalsRequest();

		request.setGeometryWkt("POINT(-77.3861 38.9696 135.0)");

		StateVector startStateVector = new StateVector();
		startStateVector.setAltitude(10000.0);
		startStateVector.setLatitude(39.0);
		startStateVector.setLongitude(-77.4);
		startStateVector.setAtTime(startTime);

		StateVector endStateVector = new StateVector();
		endStateVector.setAltitude(10030.0);
		endStateVector.setLatitude(38.98);
		endStateVector.setLongitude(-77.45);
		endStateVector.setAtTime(endTime);

		request.setStateVectors(Arrays.asList(	startStateVector,
												endStateVector));

		final ResponseEntity<GoodTimeIntervalsResponse> response = terrainController.getGoodTimeIntervals(request);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());

		final GoodTimeIntervalsResponse responseBody = response.getBody();
		Assert.assertNotNull(responseBody);

		final List<TimeInterval> timeIntervals = responseBody.getTimeIntervals();
		Assert.assertNotNull(timeIntervals);
		Assert.assertEquals(1,
							timeIntervals.size());

		Assert.assertEquals(startTime,
							timeIntervals.get(0)
									.getStart());
		Assert.assertEquals(endTime,
							timeIntervals.get(0)
									.getEnd());
	}

	@Test(expected = BadRequestException.class)
	public void testGetGoodTimeIntervalsBadGeometry() {
		final DateTime startTime = DateTime.parse("2019-05-21T18:45:00.000Z");
		final DateTime endTime = DateTime.parse("2019-05-21T18:45:10.000Z");

		GoodTimeIntervalsRequest request = new GoodTimeIntervalsRequest();

		request.setGeometryWkt("PINT(-77.3861 38.9696 135.0)");

		StateVector startStateVector = new StateVector();
		startStateVector.setAltitude(10000.0);
		startStateVector.setLatitude(39.0);
		startStateVector.setLongitude(-77.4);
		startStateVector.setAtTime(startTime);

		StateVector endStateVector = new StateVector();
		endStateVector.setAltitude(10030.0);
		endStateVector.setLatitude(38.98);
		endStateVector.setLongitude(-77.45);
		endStateVector.setAtTime(endTime);

		request.setStateVectors(Arrays.asList(	startStateVector,
												endStateVector));

		terrainController.getGoodTimeIntervals(request);
	}
}
