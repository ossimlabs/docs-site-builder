package com.maxar.common.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.maxar.common.utils.WacUtils.WACData;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

public class WacUtilsTest
{
	@Before
	public void setUp()
			throws Exception {
		WacUtils.setGridFile(this.getClass()
				.getResource("/wacgrid.txt")
				.getPath());
	}

	@Test
	public void testLatLonToWACData() {
		System.out.println("WACUtils grid: " + WacUtils.getGridFile());
		assertTrue(	"WacUtils was not initialized",
					WacUtils.getInstance()
							.isIntialized());

		int numBad = 0;
		for (double lat = -90.0; lat < 88.0; lat += 1.0) {
			for (double lon = -180.0; lon <= 180.0; lon += 1.0) {
				try {
					// create point
					final GeodeticPoint pt = GeodeticPoint.fromLatLon(	Angle.fromDegrees(lat),
																		Angle.fromDegrees(lon));

					// Use the lat/lon to determine WAC/ATC/WTM
					final WACData wacData = WacUtils.getInstance()
							.latLonToWACData(pt);
					assertNotNull(	"WACData is null for lat,lon: " + lat + "," + lon,
									wacData);

					if ((wacData.wac < 1) || (wacData.wac > 1851) || (wacData.atc < 1) || (wacData.atc > 25)
							|| (wacData.wtm < 1) || (wacData.wtm > 16)) {
						numBad++;
						// TODO: Change this back to a fail() once we fix WacUtils
						System.out.println("ERROR: Bad WAC/ATC/WTM for lat,lon: " + lat + "," + lon + " => "
								+ wacData.wac + "/" + wacData.atc + "/" + wacData.wtm);
//						fail("Bad WAC/ATC/WTM for lat,lon: " + lat + "," + lon + " => " + wacData.wac + "/" + wacData.atc + "/" + wacData.wtm);
					}
				}
				catch (final Exception e) {
					fail("Exception: " + e.getMessage() + " for lat,lon " + lat + "," + lon);
				}
			}
		}

		System.out.println("Total Bad WAC/ATC/WTM: " + numBad);
		assertTrue(	"Bad WAC/ATC/WTMs were found",
					numBad == 0);
	}

}
