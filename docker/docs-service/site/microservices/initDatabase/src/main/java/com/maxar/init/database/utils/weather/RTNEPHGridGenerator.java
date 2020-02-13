package com.maxar.init.database.utils.weather;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.maxar.common.utils.GeoUtils;
import com.maxar.weather.entity.map.MapGrid;
import com.maxar.weather.entity.map.RTNEPHQuarterGrid;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.geometry.GeodeticPolygon;

public class RTNEPHGridGenerator
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	public static List<MapGrid> generateRTNEPHQuarterGrid(
			final boolean excludeOverlap ) {

		// northern hemisphere
		final List<MapGrid> grid = generateRTNEPHGrid(	0.25,
														true,
														excludeOverlap);

		// southern hemisphere
		grid.addAll(generateRTNEPHGrid(	0.25,
										false,
										excludeOverlap));

		return grid;
	}

	public static List<MapGrid> generateRTNEPHGrid(
			final double meshSize,
			final boolean isNorth,
			final boolean excludeOverlap ) {

		final int axis = (int) (64 / meshSize);
		final int step = 1;

		final List<MapGrid> rtneph = new ArrayList<MapGrid>();

		logger.info("Generating grid: " + meshSize);

		final GeodeticPoint[][] grid = new GeodeticPoint[axis + 1][axis + 1];

		int x = 0;
		int y;

		if (isNorth) {
			final int half = axis / 2;
			for (int i = -half; i <= half; i += step) {

				y = 0;
				for (int j = half; j >= -half; j -= step) {

					grid[x][y] = calcGridMesh(	meshSize,
												i,
												j,
												isNorth);

					// logger.debug(String.format(
					// "(% 03d % 03d): %s",
					// i,
					// j,
					// grid[x][y]));

					y++;
				}
				x++;
			}
		}
		else {
			final int half = axis / 2;
			for (int i = -half; i <= half; i += step) {

				y = 0;
				for (int j = -half; j <= half; j += step) {

					grid[x][y] = calcGridMesh(	meshSize,
												i,
												j,
												isNorth);

					// logger.debug(String.format(
					// "(% 03d % 03d): %s",
					// i,
					// j,
					// grid[x][y]));

					y++;
				}
				x++;
			}
		}

		int gridNo = 0;
		int skipped = 0;

		for (y = 0; y < axis; y += step) {
			for (x = 0; x < axis; x += step) {

				gridNo++;

				// skipping anything completely in other hemisphere
				if (excludeOverlap && ((!isNorth && (grid[x][y].latitude()
						.degrees() > 0)
						&& (grid[x][y + 1].latitude()
								.degrees() > 0)
						&& (grid[x + 1][y + 1].latitude()
								.degrees() > 0)
						&& (grid[x + 1][y].latitude()
								.degrees() > 0))
						|| (isNorth && (grid[x][y].latitude()
								.degrees() < 0)
								&& (grid[x][y + 1].latitude()
										.degrees() < 0)
								&& (grid[x + 1][y + 1].latitude()
										.degrees() < 0)
								&& (grid[x + 1][y].latitude()
										.degrees() < 0)))) {
					skipped++;

					continue;
				}

				final List<GeodeticPoint> points = new ArrayList<GeodeticPoint>();

				final GeodeticPoint ul = grid[x][y];
				final GeodeticPoint ur = grid[x + 1][y];
				final GeodeticPoint lr = grid[x + 1][y + 1];
				final GeodeticPoint ll = grid[x][y + 1];

				// must adjust any point to avoid poles - need last/next
				// longitudes to cut corner
				points.addAll(GeoUtils.avoidPoles(	ul,
													ll.longitude(),
													ur.longitude()));
				points.addAll(GeoUtils.avoidPoles(	ur,
													ul.longitude(),
													lr.longitude()));
				points.addAll(GeoUtils.avoidPoles(	lr,
													ur.longitude(),
													ll.longitude()));
				points.addAll(GeoUtils.avoidPoles(	ll,
													lr.longitude(),
													ul.longitude()));

				rtneph.add(new RTNEPHQuarterGrid(
						isNorth,
						gridNo,
						GeodeticPolygon.create(points)
								.splitOnDateLine()
								.jtsGeometry_deg()));
			}
		}

		if (skipped > 0) {
			logger.info("Total skipped: " + skipped);
		}

		logger.info("Done.");

		return rtneph;
	}

	static final Length EARTH_RADIUS = Length.fromKilometers(6371.2213);
	static final Length MESH_DISTANCE = Length.fromKilometers(381);
	static final Angle REF_LATITUDE = Angle.fromDegrees(60);
	static final Angle REF_LONGITUDE = Angle.fromDegrees(10);

	static final Angle MAX_LONG = Angle.fromDegrees(180);
	static final Angle FIX_LONG = Angle.fromDegrees(360);

	static final double NORTH_SIN_PHI0 = Math.sin(REF_LATITUDE.radians());
	static final double SOUTH_SIN_PHI0 = Math.sin(-REF_LATITUDE.radians());

	private static GeodeticPoint calcGridMesh(
			final double meshSize,
			final int x,
			final int y,
			final boolean northHemisphere ) {

		final double H = (northHemisphere ? 1.0 : -1.0);
		final double TERM1 = EARTH_RADIUS.km() / (MESH_DISTANCE.km() * meshSize);
		final double TERM1_SQ = TERM1 * TERM1;
		final double TERM2 = 1 + (H * (northHemisphere ? NORTH_SIN_PHI0 : SOUTH_SIN_PHI0));
		final double TERM2_SQ = TERM2 * TERM2;
		final double TERM1X2 = TERM1_SQ * TERM2_SQ;
		final double TERM3 = (x * x) + (y * y);

		final Angle lat = Angle.fromRadians(H * Math.asin((TERM1X2 - TERM3) / (TERM1X2 + TERM3)));

		double TERM4 = Math.acos(x / Math.sqrt(TERM3));

		if (y < 0) {
			TERM4 *= -1;
		}

		Angle lon = Angle.fromRadians(REF_LONGITUDE.radians() + TERM4);

		if (lon.$greater(MAX_LONG)) {
			lon = lon.$minus(FIX_LONG);
		}

		final GeodeticPoint gp = GeodeticPoint.fromLatLon(	lat,
															lon);

		return gp;

	}

}
