package com.maxar.terrain.utils;

import org.junit.Assert;
import org.junit.Test;

import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

public class TerrainMaskNodeTest
{
	@Test
	public void testToString() {
		final Angle lat1 = Angle.fromDegrees(38.9696);
		final Angle lon1 = Angle.fromDegrees(-77.3861);
		final Length alt1 = Length.fromMeters(0.0);

		final TerrainMaskNode terrainMaskNode1 = new TerrainMaskNode(
				GeodeticPoint.fromLatLonAlt(lat1,
											lon1,
											alt1),
				Angle.fromDegrees(0.0),
				Length.fromMeters(0.0),
				Angle.fromDegrees(0.0));

		Assert.assertEquals("TerrainMaskNode: " + terrainMaskNode1.getCenter()
				.toString() + "/"
				+ terrainMaskNode1.getAzimuth()
						.toString()
				+ "/" + terrainMaskNode1.getGroundDistance()
						.toString()
				+ "/" + terrainMaskNode1.getMinGraze()
						.toString(),
							terrainMaskNode1.toString());
	}
}
