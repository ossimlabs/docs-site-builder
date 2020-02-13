package com.maxar.terrain.utils;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test_dted_invalid_dir.properties")
public class DtedUtilsTestNoValidDtedPaths
{
	@Autowired
	DtedUtils dtedUtils;

	@Test
	public void testGetInterpolatedElevation() {
		final float lat = 38.9696F;
		final float lon = -77.3861F;

		final Length dtedUtilsElev = dtedUtils.getInterpolatedElevation(lat,
																		lon);

		Assert.assertEquals(0.0,
							dtedUtilsElev.meters(),
							0.00000000001);
	}

	@Test
	public void testGenerateTerrainMask() {
		final float lat = 38.9696F;
		final float lon = -77.3861F;
		final float alt = 135.0F;

		final GeodeticPoint geodeticPoint = GeodeticPoint.fromLatLonAlt(Angle.fromDegrees(lat),
																		Angle.fromDegrees(lon),
																		Length.fromMeters(alt));

		final TerrainMask terrainMask = dtedUtils.generateTerrainMask(geodeticPoint);

		Assert.assertEquals(360,
							terrainMask.getNodes()
									.size());
		Assert.assertNotEquals(	0,
								terrainMask.getNodes()
										.size());
		for (final List<TerrainMaskNode> nodes : terrainMask.getNodes()) {
			Assert.assertFalse(nodes.isEmpty());
		}
	}
}
