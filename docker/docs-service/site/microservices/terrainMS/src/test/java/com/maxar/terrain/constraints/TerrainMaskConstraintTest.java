package com.maxar.terrain.constraints;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.maxar.terrain.utils.TerrainMask;
import com.maxar.terrain.utils.TerrainMaskNode;
import com.radiantblue.analytics.aerospace.geometry.PointToPointGeometry;
import com.radiantblue.analytics.core.constraint.IConstraintException;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

public class TerrainMaskConstraintTest
{
	private TerrainMaskConstraint terrainMaskConstraint;

	@Before
	public void setUp() {
		final TerrainMask terrainMask = new TerrainMask(
				IntStream.range(0,
								360)
						.mapToObj(deg -> Collections.singletonList(new TerrainMaskNode(
								GeodeticPoint.fromLatLonAlt(Angle.fromDegrees(38.9696),
															Angle.fromDegrees(-77.3861),
															Length.fromMeters(135.0)),
								Angle.fromDegrees(deg),
								Length.fromMeters(250.0),
								Angle.fromDegrees(0.0))))
						.collect(Collectors.toList()));

		terrainMaskConstraint = new TerrainMaskConstraint(
				terrainMask);
	}

	@Test
	public void testConstruct() {
		Assert.assertNotNull(terrainMaskConstraint);
	}

	@Test
	public void testGetName() {
		Assert.assertEquals("Terrain",
							terrainMaskConstraint.getName());
	}

	@Test
	public void testIsCombinable() {
		Assert.assertFalse(terrainMaskConstraint.isCombinable());
	}

	@Test
	public void testCombine() {
		Assert.assertNull(terrainMaskConstraint.combine(Collections.emptyList()));
	}

	@Test
	public void testIsStatic() {
		Assert.assertFalse(terrainMaskConstraint.isStatic());
	}

	@Test
	public void testIsAlwaysTrue() {
		Assert.assertFalse(terrainMaskConstraint.isAlwaysTrue());
	}

	@Test
	public void testGetFailProbability0To1() {
		Assert.assertEquals(0.5,
							terrainMaskConstraint.getFailProbability0To1(),
							0.000001);
	}

	@Test
	public void testGetLongComputationProbability() {
		Assert.assertEquals(0.5,
							terrainMaskConstraint.getLongComputationProbability0to1(),
							0.000001);
	}

	@Test
	public void testCheckSuccess() {
		final DateTime atTime = DateTime.parse("2019-05-21T18:45:00.000Z");

		final GeodeticPoint sourceGeodeticPoint = GeodeticPoint.fromLatLonAlt(	Angle.fromDegrees(39.0),
																				Angle.fromDegrees(-77.4),
																				Length.fromMeters(10000.0));

		final GeodeticPoint destinationGeodeticPoint = GeodeticPoint.fromLatLonAlt(	Angle.fromDegrees(38.9696),
																					Angle.fromDegrees(-77.3861),
																					Length.fromMeters(135.0));

		final PointToPointGeometry pointToPointGeometry = PointToPointGeometry.create(	sourceGeodeticPoint,
																						destinationGeodeticPoint,
																						atTime);

		Assert.assertNull(terrainMaskConstraint.check(pointToPointGeometry));
	}

	@Test
	public void testCheckFail() {
		final DateTime atTime = DateTime.parse("2019-05-21T18:45:00.000Z");

		final GeodeticPoint sourceGeodeticPoint = GeodeticPoint.fromLatLonAlt(	Angle.fromDegrees(39.0),
																				Angle.fromDegrees(-77.4),
																				Length.fromMeters(1.0));

		final GeodeticPoint destinationGeodeticPoint = GeodeticPoint.fromLatLonAlt(	Angle.fromDegrees(38.9696),
																					Angle.fromDegrees(-77.3861),
																					Length.fromMeters(135.0));

		final PointToPointGeometry pointToPointGeometry = PointToPointGeometry.create(	sourceGeodeticPoint,
																						destinationGeodeticPoint,
																						atTime);

		final IConstraintException exception = terrainMaskConstraint.check(pointToPointGeometry);
		Assert.assertNotNull(exception);
		Assert.assertEquals("TerrainConstraint out of bounds",
							exception.getMessage());
	}
}
