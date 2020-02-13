package com.maxar.terrain.utils;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.bbn.openmap.dataAccess.dted.DTEDFrame;
import com.bbn.openmap.dataAccess.dted.DTEDFrameUtil;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test_dted_one_valid_dir.properties")
public class DtedUtilsTestOneValidDtedPath
{
	@Autowired
	DtedUtils dtedUtils;

	@Value("#{'${microservices.terrain.dtedDirPaths}'.split(',')}")
	private List<String> dtedDirPaths = null;

	@Test
	public void testGetInterpolatedElevation() {
		final float lat = 38.9696F;
		final float lon = -77.3861F;

		final Length dtedUtilsElev = dtedUtils.getInterpolatedElevation(lat,
																		lon);

		final String dtedLonDir = dtedDirPaths.get(0) + "/" + DTEDFrameUtil.lonToFileString(lon);
		final String dtedFile = dtedLonDir + "/" + DTEDFrameUtil.latToFileString(	lat,
																					0);

		final DTEDFrame frame = new DTEDFrame(
				dtedFile);

		final Length dtedElev = Length.fromMeters(frame.interpElevationAt(	lat,
																			lon));

		Assert.assertEquals(dtedElev.meters(),
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
