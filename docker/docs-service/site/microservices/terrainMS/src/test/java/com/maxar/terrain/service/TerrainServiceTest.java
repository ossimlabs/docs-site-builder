package com.maxar.terrain.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Before;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.terrain.utils.DtedUtils;
import com.maxar.terrain.utils.TerrainMask;
import com.maxar.terrain.utils.TerrainMaskNode;
import com.maxar.terrain.model.StateVector;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test_terrainms.properties")
public class TerrainServiceTest
{
	@Autowired
	TerrainService terrainService;

	@MockBean
	DtedUtils dtedUtils;

	@Before
	public void setUp() {
		Mockito.when(dtedUtils.generateTerrainMask(ArgumentMatchers.any(GeodeticPoint.class)))
				.thenReturn(new TerrainMask(
						IntStream.range(0,
										360)
								.mapToObj(deg -> Collections.singletonList(new TerrainMaskNode(
										GeodeticPoint.fromLatLonAlt(Angle.fromDegrees(38.9696),
																	Angle.fromDegrees(-77.3861),
																	Length.fromMeters(135.0)),
										Angle.fromDegrees(deg),
										Length.fromMeters(250.0),
										Angle.fromDegrees(0.0))))
								.collect(Collectors.toList())));
	}

	@Test
	public void testGetGoodTimeIntervalsPointOneGoodInterval() {
		final String geometryWkt = "POINT(-77.3861 38.9696 135.0)";
		final DateTime startTime = DateTime.parse("2019-05-21T18:45:00.000Z");
		final DateTime endTime = DateTime.parse("2019-05-21T18:45:10.000Z");
		WKTReader reader = new WKTReader();

		try {
			final Geometry geometry = reader.read(geometryWkt);

			StateVector startStateVector = new StateVector();
			startStateVector.setAtTime(startTime);
			startStateVector.setLatitude(39.0);
			startStateVector.setLongitude(-77.4);
			startStateVector.setAltitude(10000.0);

			StateVector endStateVector = new StateVector();
			endStateVector.setAtTime(endTime);
			endStateVector.setLatitude(38.98);
			endStateVector.setLongitude(-77.45);
			endStateVector.setAltitude(10030.0);

			final List<Interval> goodTimeIntervals = terrainService.getGoodTimeIntervals(	geometry,
																							Arrays.asList(	startStateVector,
																											endStateVector));

			Assert.assertEquals(1,
								goodTimeIntervals.size());

			Assert.assertEquals(startTime,
								goodTimeIntervals.get(0)
										.getStart());
			Assert.assertEquals(endTime,
								goodTimeIntervals.get(0)
										.getEnd());
		}
		catch (final ParseException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetGoodTimeIntervalsPointNoGoodIntervals() {
		final String geometryWkt = "POINT(-77.3861 38.9696 135.0)";
		final DateTime startTime = DateTime.parse("2019-05-21T18:45:00.000Z");
		final DateTime endTime = DateTime.parse("2019-05-21T18:45:10.000Z");
		WKTReader reader = new WKTReader();

		try {
			final Geometry geometry = reader.read(geometryWkt);

			StateVector startStateVector = new StateVector();
			startStateVector.setAtTime(startTime);
			startStateVector.setLatitude(39.0);
			startStateVector.setLongitude(-77.4);
			startStateVector.setAltitude(1.0);

			StateVector endStateVector = new StateVector();
			endStateVector.setAtTime(endTime);
			endStateVector.setLatitude(38.98);
			endStateVector.setLongitude(-77.45);
			endStateVector.setAltitude(5.0);

			final List<Interval> goodTimeIntervals = terrainService.getGoodTimeIntervals(	geometry,
																							Arrays.asList(	startStateVector,
																											endStateVector));

			Assert.assertTrue(goodTimeIntervals.isEmpty());
		}
		catch (final ParseException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetGoodTimeIntervalsSplitGoodIntervals() {
		final String geometryWkt = "POINT(-77.3861 38.9696 135.0)";
		final DateTime interval0StartTime = DateTime.parse("2019-05-21T18:45:00.000Z");
		final DateTime interval0EndTime = DateTime.parse("2019-05-21T18:45:02.000Z");
		final DateTime interval1StartTime = DateTime.parse("2019-05-21T18:45:06.000Z");
		final DateTime interval1EndTime = DateTime.parse("2019-05-21T18:45:10.000Z");
		WKTReader reader = new WKTReader();

		try {
			final Geometry geometry = reader.read(geometryWkt);

			StateVector stateVector0 = new StateVector();
			stateVector0.setAtTime(interval0StartTime);
			stateVector0.setLatitude(39.0);
			stateVector0.setLongitude(-77.4);
			stateVector0.setAltitude(10000.0);

			StateVector stateVector1 = new StateVector();
			stateVector1.setAtTime(interval0EndTime);
			stateVector1.setLatitude(38.99);
			stateVector1.setLongitude(-77.42);
			stateVector1.setAltitude(9000.0);

			StateVector stateVector2 = new StateVector();
			stateVector2.setAtTime(DateTime.parse("2019-05-21T18:45:04.000Z"));
			stateVector2.setLatitude(38.98);
			stateVector2.setLongitude(-77.45);
			stateVector2.setAltitude(5.0);

			StateVector stateVector3 = new StateVector();
			stateVector3.setAtTime(interval1StartTime);
			stateVector3.setLatitude(38.97);
			stateVector3.setLongitude(-77.47);
			stateVector3.setAltitude(8500.0);

			StateVector stateVector4 = new StateVector();
			stateVector4.setAtTime(DateTime.parse("2019-05-21T18:45:08.000Z"));
			stateVector4.setLatitude(38.96);
			stateVector4.setLongitude(-77.5);
			stateVector4.setAltitude(9500.0);

			StateVector stateVector5 = new StateVector();
			stateVector5.setAtTime(interval1EndTime);
			stateVector5.setLatitude(38.95);
			stateVector5.setLongitude(-77.52);
			stateVector5.setAltitude(10030.0);

			final List<Interval> goodTimeIntervals = terrainService.getGoodTimeIntervals(	geometry,
																							Arrays.asList(	stateVector0,
																											stateVector1,
																											stateVector2,
																											stateVector3,
																											stateVector4,
																											stateVector5));

			Assert.assertEquals(2,
								goodTimeIntervals.size());

			Assert.assertEquals(interval0StartTime,
								goodTimeIntervals.get(0)
										.getStart());
			Assert.assertEquals(interval0EndTime,
								goodTimeIntervals.get(0)
										.getEnd());

			Assert.assertEquals(interval1StartTime,
								goodTimeIntervals.get(1)
										.getStart());
			Assert.assertEquals(interval1EndTime,
								goodTimeIntervals.get(1)
										.getEnd());
		}
		catch (final ParseException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetGoodTimeIntervalsPolygonOneGoodInterval() {
		final String geometryWkt = "POLYGON((-77.3920 38.9710 133.5, -77.3920 38.9508 136.2, -77.3735 38.9508 135.8, "
				+ "-77.3735 38.9710 134.1, -77.3920 38.9710 133.5))";
		final DateTime startTime = DateTime.parse("2019-05-21T18:45:00.000Z");
		final DateTime endTime = DateTime.parse("2019-05-21T18:45:10.000Z");
		WKTReader reader = new WKTReader();

		try {
			final Geometry geometry = reader.read(geometryWkt);

			StateVector startStateVector = new StateVector();
			startStateVector.setAtTime(startTime);
			startStateVector.setLatitude(39.0);
			startStateVector.setLongitude(-77.4);
			startStateVector.setAltitude(10000.0);

			StateVector endStateVector = new StateVector();
			endStateVector.setAtTime(endTime);
			endStateVector.setLatitude(38.98);
			endStateVector.setLongitude(-77.45);
			endStateVector.setAltitude(10030.0);

			final List<Interval> goodTimeIntervals = terrainService.getGoodTimeIntervals(	geometry,
																							Arrays.asList(	startStateVector,
																											endStateVector));

			Assert.assertEquals(1,
								goodTimeIntervals.size());

			Assert.assertEquals(startTime,
								goodTimeIntervals.get(0)
										.getStart());
			Assert.assertEquals(endTime,
								goodTimeIntervals.get(0)
										.getEnd());
		}
		catch (final ParseException e) {
			Assert.fail(e.getMessage());
		}
	}
}
