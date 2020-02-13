package com.maxar.terrain.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.bbn.openmap.layer.dted.DTEDFrameCache;
import com.bbn.openmap.layer.dted.DTEDSubframedFrame;
import com.bbn.openmap.util.Debug;
import com.radiantblue.analytics.aerospace.geometry.PointToPointGeometry;
import com.radiantblue.analytics.core.akka.ClusterSystem;
import com.radiantblue.analytics.core.akka.RequestParameters;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

/**
 * Contains utility methods for processing Digital Terrain Elevation Data
 * (DTED).
 */
@Component
public class DtedUtils
{
	/**
	 * Number of azimuths needed for terrainMask
	 */
	private static final int NUM_AZIMUTHS = 360;

	/**
	 * The value DTED frames will have if they contain no elevation data.
	 */
	private static final int DTED_NO_DATA_VALUE = -32767;

	/**
	 * The logger.
	 */
	private static Logger logger = SourceLogger.getLogger(DtedUtils.class.getName());

	/**
	 * Set to false on startup if there are no valid DTED directories specified.
	 */
	private boolean dtedSetupIsValid = true;

	/**
	 * The DTED frame cache.
	 */
	private DTEDFrameCache dtedFrameCache = null;

	/**
	 * The cluster system.
	 */
	@Autowired
	private ClusterSystem clusterSystem;

	@Value("#{'${microservices.terrain.dtedDirPaths}'.split(',')}")
	private List<String> dtedDirPaths = null;

	@Value("${microservices.terrain.cacheFrameSize:100}")
	private int cacheFrameSize;

	@Value("${microservices.terrain.dtedLevel:0}")
	private int dtedLevel;

	@Value("${microservices.terrain.stepSizeMeters:250}")
	private double stepSizeMeters;

	@Value("${microservices.terrain.numSteps:100}")
	private int numSteps;

	@Value("${microservices.terrain.packetSize:10}")
	private int packetSize;

	/**
	 * Validates the DTED directory paths and creates the frame cache.
	 */
	@EventListener(ContextRefreshedEvent.class)
	private void initializeDtedDirPaths() {
		// We can now pass multiple directory paths into a DTED instance. Check
		// each of the paths to make sure we have at least one valid path to
		// use.
		final int dtedDirPathsSize = dtedDirPaths == null ? 0 : dtedDirPaths.size();
		final List<String> realDtedPaths = (dtedDirPaths == null ? new ArrayList<String>() : dtedDirPaths).stream()
				.filter(path -> {
					final File dirPathTest = new File(
							path);
					if (!dirPathTest.exists()) {
						logger.warn("Invalid DTED path in config [" + path + "]");
						return false;
					}
					return true;
				})
				.collect(Collectors.toList());

		// If we have at least one valid path, we are good to go
		if (!realDtedPaths.isEmpty()) {
			// Check to see if the original number of paths is equal to the real
			// number of valid paths. If they are not, we need to reset the
			// paths to the correct number.
			if (dtedDirPaths == null || realDtedPaths.size() != dtedDirPathsSize) {
				dtedDirPaths = realDtedPaths;
			}
			// TODO: We may need to verify the existence of a "flat DTED"
			// directory which is what the openmap code is expecting to find.

			// If we make it here, all is good (maybe!). So create the frame
			// cache instance
			dtedFrameCache = new DTEDFrameCache(
					dtedDirPaths.toArray(new String[0]),
					cacheFrameSize);
			logger.debug("DTEDUtils has been configured successfully.");
			dtedSetupIsValid = true;
		}
		else {
			logger.debug("There are no valid paths for DTED. "
					+ "Set the 'DtedUtils.dtedDirPaths' property in the application "
					+ "you are running to point to valid DTED paths (levels 0,1,2) "
					+ "Use a semicolon separator between these paths.");
			dtedSetupIsValid = false;
		}

		Debug.remove("all");
		Debug.remove("debugAll");
	}

	/**
	 * Gets the interpolated elevation of a point based on the loaded DTED.
	 *
	 * @param lat
	 *            The latitude of the point, in degrees.
	 * @param lon
	 *            The longitude of the point, in degrees.
	 * @return The interpolated elevation of the point.
	 */
	public Length getInterpolatedElevation(
			final double lat,
			final double lon ) {
		// Default return value to zero
		Length ret = Length.Zero();

		// If we have setup the DTED paths correctly (at least 1), we will have
		// a frame cache to use for interpolation. If not, only 0.0 will be
		// returned.
		if (dtedSetupIsValid) {
			ret = getInterpolatedElevation(	dtedFrameCache,
											lat,
											lon);
		}
		else {
			logger.debug("DTED paths not configured - returning 0.0 for all elevations");
		}

		return ret;
	}

	/**
	 * Gets the interpolated elevation of a point based on the loaded DTED.
	 *
	 * @param dtedFrameCache
	 *            The DTED frame cache to use for interpolation.
	 * @param lat
	 *            The latitude of the point, in degrees.
	 * @param lon
	 *            The longitude of the point, in degrees.
	 * @return The interpolated elevation of the point.
	 */
	private Length getInterpolatedElevation(
			final DTEDFrameCache dtedFrameCache,
			final double lat,
			final double lon ) {
		int level = dtedLevel;

		// Default to 0
		Length elev = Length.Zero();

		// Default to null
		DTEDSubframedFrame subFrame = null;

		// *** NEW *** Try all levels of DTED before punting. This assumes that
		// we have passed in more than 1 DTED path in the DtedUtils constructor.

		// Loop through all available DTED levels until we find a subframe for
		// this location at the requested level.
		while ((level >= 0) && (subFrame == null)) {
			if ((subFrame = dtedFrameCache.get(	lat,
												lon,
												level)) == null) {
				// Subframe is not found for the current level.
				// Try the next "lesser level"
				--level;
			}
		}

		// we are missing some dted 0 frame...northern hem files in the
		// w111 directory
		if (subFrame != null) {
			final int elevMeters = subFrame.interpElevationAt(	(float) lat,
																(float) lon);

			if (elevMeters != DTED_NO_DATA_VALUE) {
				elev = Length.fromMeters(elevMeters);
				logger.debug("DTED " + level + " returned elevation " + elev.toString() + " at " + latLonToString(	lat,
																													lon));
			}
			else {
				// Want to log this so that we know where the holes are...
				logger.warn("DTED " + level + " had void at " + latLonToString(	lat,
																				lon)
						+ " Returning elevation of 0.0");
			}
		}
		else {
			// Want to log this so that we know where the holes are...
			logger.warn("DTED (0, 1, 2) had void at " + latLonToString(	lat,
																		lon)
					+ " Returning elevation of 0.0");
		}

		return elev;
	}

	/**
	 * Generate a terrain mask for a point based on the loaded DTED.
	 *
	 * @param center
	 *            The center point of the target to generate the terrain mask for.
	 * @return The terrain mask for the specified point.
	 */
	public TerrainMask generateTerrainMask(
			final GeodeticPoint center ) {

		logger.debug("Generating terrain mask: " + center + ": " + stepSizeMeters + "/" + numSteps);

		// init results arrays
		final List<List<TerrainMaskNode>> mask = new ArrayList<>();

		// Create the empty lists for each azimuth
		for (int i = 0; i < NUM_AZIMUTHS; i++) {
			mask.add(new ArrayList<>());
		}

		// List of azimuths/offsets to calculate in each work unit
		final List<TerrainMaskNode> nodes = new ArrayList<>();

		// Loop for NUM_AZIMUTHS azimuths
		for (int az = 0; az < NUM_AZIMUTHS; az++) {
			// Create an "Angle" from the integer azimuth
			final Angle azimuth = Angle.fromDegrees(az);

			// Default this to 0 - start at 0 length from the location
			Length sampleDistance = Length.Zero();

			// This inner loop will execute for "numSteps" from the
			// configuration file. In our test, that's 50 steps.
			for (int step = 0; step < numSteps; step++) {
				// Calculate the next sample distance - which is where the last
				// sampleDistance was plus the stepSizeMeters value from the
				// config file.
				sampleDistance = sampleDistance.plus(Length.fromMeters(stepSizeMeters));

				// Create a new node for this center
				// point/azimuth/sampleDistance
				final TerrainMaskNode node = new TerrainMaskNode(
						center,
						azimuth,
						sampleDistance,
						Angle.Zero());

				// This adds this new node to the overall nodes list
				nodes.add(node);
			}
		}

		final long startTime = System.currentTimeMillis();

		logger.debug("Total samples (nodes): " + nodes.size());

		// Use AKKA clusters to do work in parallel
		final List<TerrainMaskNode> scalaNodes = clusterSystem.requestWork_J(	nodes,
																				new RequestParameters(
																						packetSize,
																						1),
																				nodes1 -> {
																					// new cache for each worker to
																					// prevent DTED library
																					// issues
																					final DTEDFrameCache workerDtedFrameCache = new DTEDFrameCache(
																							dtedDirPaths
																									.toArray(new String[0]),
																							cacheFrameSize);

																					for (final TerrainMaskNode node : nodes1) {
																						GeodeticPoint gp = center
																								.move(	node.getAzimuth(),
																										node.getGroundDistance());

																						// calculate minimum graze at
																						// sample point
																						final Length elevation = getInterpolatedElevation(	workerDtedFrameCache,
																																			gp.latitude()
																																					.degrees(),
																																			gp.longitude()
																																					.degrees());

																						logger.debug("ELEV (m): "
																								+ elevation.meters());

																						// add altitude
																						gp = GeodeticPoint
																								.fromLatLonAlt(	gp
																										.latitude(),
																												gp.longitude(),
																												elevation);

																						// set nodes position
																						node.setCenter(gp);

																						// Using NULL date since does
																						// not matter to calculate graze
																						final PointToPointGeometry ptpg = PointToPointGeometry
																								.create(gp,
																										center,
																										null);

																						// set minGraze
																						node.setMinGraze(ptpg
																								.grazingAngle_atDest());
																					}

																					// return same list... with
																					// minGrazes filled in
																					return nodes1;
																				});

		// need to convert to pure Java list to sort the collection
		final List<TerrainMaskNode> finalNodes = new ArrayList<>(
				scalaNodes);

		// Combine data and set mask

		// sort by azimuth/ground distance
		finalNodes.sort((
				o1,
				o2 ) -> {
			if (o1.getAzimuth()
					.degrees() != o2.getAzimuth()
							.degrees()) {
				return (int) (o1.getAzimuth()
						.degrees()
						- o2.getAzimuth()
								.degrees());
			}

			return (int) (o1.getGroundDistance()
					.meters()
					- o2.getGroundDistance()
							.meters());
		});

		TerrainMaskNode lastNode = null;
		double maxMinGrazeDegs = -90.0;

		// go through results and set minGrazes by azimuth
		for (final TerrainMaskNode node : finalNodes) {
			// Add the 0.5 to this number because we are about to truncate it to
			// an integer to use it as an index. Not a good practice to use
			// doubles as indices.
			final int azDeg = (int) Math.round(node.getAzimuth()
					.degrees());

			// If new azimuth, reset maxMinGraze - We need to use the Math.round
			// method's result casted to an int to make these double azimuths
			// into usable integer indices.
			if ((lastNode == null) || (azDeg != (int) (Math.round(lastNode.getAzimuth()
					.degrees())))) {
				logger.debug("-----RESETTING mmaxMinDegs for new Azimuth " + azDeg);
				maxMinGrazeDegs = -90.0;
			}

			logger.debug("-----COMPARING [AZ: " + azDeg + "], " + node.getMinGraze()
					.degrees() + " vs " + maxMinGrazeDegs);

			// track MAX minGraze per azimuth
			if (node.getMinGraze()
					.degrees() > maxMinGrazeDegs) {
				maxMinGrazeDegs = node.getMinGraze()
						.degrees();

				// only add to mask if minGraze greater than previous
				mask.get(azDeg)
						.add(node);

				logger.debug("MASK: " + azDeg + ": " + maxMinGrazeDegs);
				logger.debug("    ### " + node.getAzimuth() + "/" + node.getGroundDistance() + "/"
						+ node.getMinGraze());
			}

			lastNode = node;
		}

		final long endTime = System.currentTimeMillis();
		final Duration duration = new Duration(
				startTime,
				endTime);

		final double durationSecs = duration.getMillis() / 1000.0;
		logger.debug("Terrain mask completed (sec): " + durationSecs);
		logger.debug("Samples/second: " + (nodes.size() / durationSecs));

		return new TerrainMask(
				mask);
	}

	private static String latLonToString(
			final double lat,
			final double lon ) {
		return "lat=" + lat + ", lon=" + lon;
	}
}
