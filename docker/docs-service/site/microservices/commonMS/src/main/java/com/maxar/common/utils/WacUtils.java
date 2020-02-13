package com.maxar.common.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.log4j.Logger;

import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.geometry.GeodeticPolygon;

public class WacUtils
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	// From BlueBox

	public class WACData
	{
		public final int wac;
		public final int atc;
		public final int wtm;

		public WACData(
				final int wac,
				final int atc,
				final int wtm ) {
			this.wac = wac;
			this.atc = atc;
			this.wtm = wtm;
		}

		@Override
		public boolean equals(
				final Object obj ) {
			if (!(obj instanceof WACData)) {
				return false;
			}

			final WACData other = (WACData) obj;

			return (wac == other.wac) && (atc == other.atc) && (wtm == other.wtm);
		}
	}

	public enum GridType {
		WAC,
		ATC,
		WTM,
		SUBCELL
	}

	public NumberFormat nf = NumberFormat.getNumberInstance();
	private static final int lltowac[][] = new int[45][360];
	private static final int wactoll[][] = new int[1851][2]; // Gives the
																// Lat-Long of
	// the WAC's northwest
	// corner

	private boolean init = false;

	private static WacUtils instance;
	private static String gridFile;

	private WacUtils(
			final String gridFilePath ) {
		init = loadGridMaps(gridFilePath);
	}

	private WacUtils() {}

	public static synchronized WacUtils getInstance() {
		if (instance == null) {
			if (gridFile == null) {
				// TODO: Just log an error or throw an exception or both?
				logger.error("Attempting to use WacUtils without a GridFile!");
			}
			instance = new WacUtils(
					gridFile);
		}

		return instance;
	}

	public boolean isIntialized() {
		return init;
	}

	public static synchronized String getGridFile() {
		return gridFile;
	}

	public static synchronized void setGridFile(
			final String gridFilename ) {
		gridFile = gridFilename;
	}

	public static boolean inRangeInclusive(
			final double value,
			final double min,
			final double max ) {
		return ((value >= min) && (value <= max));
	}

	private boolean loadGridMaps(
			final String gridFilePath ) {

		logger.info("Loading wac grid file: " + gridFilePath);

		String line;

		try (final BufferedReader br = new BufferedReader(
				new FileReader(
						gridFilePath))) {

			// eat 1st line for lltowac: lltowac[45][360] = {
			line = br.readLine();
			int xIndex = 0;
			int yIndex = 0;

			while ((line = br.readLine()) != null) {
				line = line.trim();

				if (line.length() == 0) { // skip blank lines
					continue;
				}
				else if (line.startsWith("wactoll")) { // start of wactoll
					// section
					break;
				}
				else if (line.contains("}")) { // end of x segment
					xIndex++;
					yIndex = 0;
					continue;
				}
				else if (line.startsWith("{")) { // start of x segment
					line = line.substring(1);
				}

				final String[] tokens = line.split(",");
				for (final String element : tokens) {
					lltowac[xIndex][yIndex++] = Integer.parseInt(element.trim());
				}
			}

			// wactoll section: wactoll[1851][2] = {
			xIndex = 0;

			while ((line = br.readLine()) != null) {
				line = line.trim();

				if (line.length() == 0) { // skip blank lines
					continue;
				}
				else if (line.contains("};")) { // end of section
					break;
				}

				final int end = line.indexOf("}");

				line = line.substring(	1,
										end);

				final String[] tokens = line.split(",");
				for (int i = 0; i < tokens.length; i++) {
					wactoll[xIndex][i] = Integer.parseInt(tokens[i].trim());
				}

				xIndex++;
			}

		}
		catch (final FileNotFoundException fe) {
			final String errMsg = "Cannot open wac grid file: '" + gridFilePath + "'";
			logger.warn(errMsg,
						fe);
			return false;
		}
		catch (final IOException ioe) {
			final String errMsg = "error parsing data in wac grid config file '" + gridFilePath + "'";
			logger.warn(errMsg,
						ioe);
			return false;
		}

		return true;
	}

	public WACData parseWAG(
			final String wagID ) {
		// Check for blank input
		if (!init || (wagID == null) || (wagID.length() == 0)) {
			return null;
		}

		// Strip blanks and tabs
		final String tmp = wagID.trim();
		final int len = tmp.length();

		// Check the length of the id
		if ((len < 4) || (len > 8)) {
			return null;
		}

		// Check for non-digit characters
		try {
			Integer.parseInt(tmp);
		}
		catch (final Exception e) {
			return null;
		}

		String wacStr = null;
		String atcStr = null;
		String wtmStr = null;

		// Parse it up
		if (len < 5) {
			wacStr = tmp;
		}
		else if (len < 6) {
			wacStr = tmp.substring(	0,
									3);
			atcStr = tmp.substring(	3,
									5);
		}
		else if (len < 7) {
			wacStr = tmp.substring(	0,
									4);
			atcStr = tmp.substring(	4,
									6);
		}
		else if (len < 8) {
			wacStr = tmp.substring(	0,
									3);
			atcStr = tmp.substring(	3,
									5);
			wtmStr = tmp.substring(	5,
									7);
		}
		else if (len < 9) {
			wacStr = tmp.substring(	0,
									4);
			atcStr = tmp.substring(	4,
									6);
			wtmStr = tmp.substring(	6,
									8);
		}

		int wac = 0;
		int atc = 0;
		int wtm = 0;

		try {
			wac = Integer.parseInt(wacStr);
			atc = Integer.parseInt(atcStr);
			wtm = Integer.parseInt(wtmStr);
		}
		catch (final Exception e) {
			return null;
		}

		// Validate the ranges
		if (inRangeInclusive(	wac,
								1,
								1851)
				&& ((len < 5) || inRangeInclusive(	atc,
													0,
													25))
				&& ((len < 9) || inRangeInclusive(	wtm,
													0,
													16))) {
			return new WACData(
					wac,
					atc,
					wtm);
		}

		return null;
	}

	public GeodeticPoint[] getCorners(
			final String wagID ) {
		// Parse the WAG ID into its parts
		final WACData wacData = parseWAG(wagID);

		if (wacData == null) {
			return null;
		}

		return getCorners(wacData);
	}

	public GeodeticPoint[] getCorners(
			final WACData wacData ) {
		// Get the corner coordinates
		final GeodeticPoint[] wacCorners = getWACCorners(wacData.wac);
		double nwLat = wacCorners[0].latitude()
				.degrees();
		double nwLon = wacCorners[0].longitude()
				.degrees();
		double seLat = wacCorners[1].latitude()
				.degrees();
		double seLon = wacCorners[1].longitude()
				.degrees();

		if (wacData.atc > 0) {
			final Point2d[] atcOffsets = getATCOffsets(	wacData.wac,
														wacData.atc);
			if (atcOffsets != null) {
				seLat = nwLat + atcOffsets[1].y;
				seLon = nwLon + atcOffsets[1].x;
				nwLat += atcOffsets[0].y;
				nwLon += atcOffsets[0].x;
			}
		}

		if (wacData.wtm > 0) {
			final Point2d[] wtmOffsets = getWTMOffsets(	wacData.wac,
														wacData.wtm);
			if (wtmOffsets != null) {
				seLat = nwLat + wtmOffsets[1].y;
				seLon = nwLon + wtmOffsets[1].x;
				nwLat += wtmOffsets[0].y;
				nwLon += wtmOffsets[0].x;
			}
		}
		// Normalize
		if (nwLon > 180.0) {
			nwLon -= 360.0;
		}
		if (seLon > 180.0) {
			seLon -= 360.0;
		}
		if ((nwLon == 180.0) && (seLon < 0.0)) {
			nwLon = -180.0;
		}
		else if ((seLon == 180.0) && (nwLon < 0.0)) {
			seLon = -180.0;
		}

		final GeodeticPoint[] corners = new GeodeticPoint[2];
		corners[0] = GeodeticPoint.fromLatLon(	Angle.fromDegrees(nwLat),
												Angle.fromDegrees(nwLon));
		corners[1] = GeodeticPoint.fromLatLon(	Angle.fromDegrees(seLat),
												Angle.fromDegrees(seLon));

		return corners;
	}

	public String latLonToWAG(
			final GeodeticPoint ll,
			final int digits ) {
		// Validate the inputs
		if (digits < 4) {
			return null;
		}

		final WACData wacData = latLonToWACData(ll);
		if (wacData == null) {
			return null;
		}
		return wacDataToWAG(wacData,
							digits);
	}

	public String wacDataToWAG(
			final WACData wacData,
			final int digits ) {
		// Build the WAG
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
		nf.setMaximumIntegerDigits(4);
		nf.setMinimumIntegerDigits(4);
		nf.setGroupingUsed(false);
		final String wacStr = nf.format(wacData.wac);

		nf.setMinimumIntegerDigits(2);
		final String atcStr = nf.format(wacData.atc);
		final String wtmStr = nf.format(wacData.wtm);
		String tmp = wacStr;
		if (digits > 5) {
			tmp += atcStr;
		}

		if (digits > 7) {
			tmp += wtmStr;
		}

		return tmp;
	}

	public GeodeticPoint[] getWACCorners(
			final int wac ) {
		// Validate the WAC
		if (!inRangeInclusive(	wac,
								1,
								1851)) {
			return null;
		}

		// Get the NW coordinates from the look-up table
		final double nwLat = wactoll[wac - 1][0];
		double nwLon = wactoll[wac - 1][1];
		if (nwLon > 180.0) {
			nwLon -= 360.0;
		}

		// Get the SE coordinates from the look-up table

		final double w = getWACWidth(wac);
		final double seLat = wactoll[wac - 1][0] - 4.0;
		double seLon = wactoll[wac - 1][1] + w;

		if (seLon > 360.0) {
			seLon -= 360.0;
		}

		if (seLon > 180.0) {
			seLon -= 360.0;
		}

		final GeodeticPoint[] corners = new GeodeticPoint[2];
		corners[0] = GeodeticPoint.fromLatLon(	Angle.fromDegrees(nwLat),
												Angle.fromDegrees(nwLon));
		corners[1] = GeodeticPoint.fromLatLon(	Angle.fromDegrees(seLat),
												Angle.fromDegrees(seLon));
		return corners;
	}

	public double getWACWidth(
			final int wac ) {
		// Validate the WAC
		if (!inRangeInclusive(	wac,
								1,
								1851)) {
			return 0.0;
		}

		// Calculate the look-up table index
		final int r = Math.abs(wactoll[wac - 1][0] - 88) / 4;

		// Count the number of WAC references in this row
		int matches = 0;
		for (int i = 0; i < 360; i++) {
			if (lltowac[r][i] == wac) {
				matches++;
			}
		}

		return matches;
	}

	public double getATCWidth(
			final int wac ) {
		return getWACWidth(wac) * 0.2;
	}

	public double getWTMWidth(
			final int wac ) {
		return getWACWidth(wac) * 0.05;
	}

	public double getSubcellWidth(
			final int wac ) {
		return getWTMWidth(wac) / 6;
	}

	public Point2d[] getATCOffsets(
			final int wac,
			final int atc ) {
		// Validate the WAC and ATC
		if (!inRangeInclusive(	wac,
								1,
								1851)) {
			return null;
		}

		if (!inRangeInclusive(	atc,
								1,
								25)) {
			return null;
		}

		// Calculate the NW offset
		final double w = getATCWidth(wac);
		final int r = (atc - 1) / 5;
		final int c = atc - 1 - (r * 5);
		final double nwLatOffset = r * -0.8;
		final double nwLonOffset = c * w;

		// Calculate the SE offset
		final double seLatOffset = nwLatOffset - 0.8;
		final double seLonOffset = nwLonOffset + w;

		final Point2d[] corners = new Point2d[2];
		corners[0] = new Point2d(
				nwLonOffset,
				nwLatOffset);
		corners[1] = new Point2d(
				seLonOffset,
				seLatOffset);

		return corners;
	}

	public Point2d[] getWTMOffsets(
			final int wac,
			final int wtm ) {
		// Validate the WAC and WTM
		if (!inRangeInclusive(	wac,
								1,
								1851)) {
			return null;
		}

		if (!inRangeInclusive(	wtm,
								1,
								16)) {
			return null;
		}

		// Calculate the NW offset
		final double w = getWTMWidth(wac);
		final int r = (wtm - 1) / 4;
		final int c = wtm - 1 - (r * 4);
		final double nwLatOffset = r * -0.2;
		final double nwLonOffset = c * w;

		// Calculate the SE offset
		final double seLatOffset = nwLatOffset - 0.2;
		final double seLonOffset = nwLonOffset + w;

		final Point2d[] corners = new Point2d[2];
		corners[0] = new Point2d(
				nwLonOffset,
				nwLatOffset);
		corners[1] = new Point2d(
				seLonOffset,
				seLatOffset);

		return corners;
	}

	public Point2d[] getSubcellOffsets(
			final int wac,
			final int subcell ) {
		// Validate the WAC and WTM
		if (!inRangeInclusive(	wac,
								1,
								1851)) {
			return null;
		}

		if (!inRangeInclusive(	subcell,
								1,
								25)) {
			return null;
		}

		// Calculate the NW offset
		final double w = getSubcellWidth(wac);
		final int r = (subcell - 1) / 6;
		final int c = subcell - 1 - (r * 6);
		final double nwLatOffset = r * -0.05;
		final double nwLonOffset = c * w;

		// Calculate the SE offset
		final double seLatOffset = nwLatOffset - 0.05;
		final double seLonOffset = nwLonOffset + w;

		final Point2d[] corners = new Point2d[2];
		corners[0] = new Point2d(
				nwLonOffset,
				nwLatOffset);
		corners[1] = new Point2d(
				seLonOffset,
				seLatOffset);

		return corners;
	}

	public WACData latLonToWACData(
			final GeodeticPoint ll ) {
		int deltaLat, deltaLon;
		int r, c;

		final DecimalFormat f = new DecimalFormat(
				"####.######");

		double lat = Double.parseDouble(f.format(ll.latitude()
				.degrees()));
		double lon = Double.parseDouble(f.format(ll.longitude()
				.degrees()));
		final double originalLon = lon;

		// Validate the inputs
		if (!inRangeInclusive(	lat,
								-90.0,
								88.0)
				|| !inRangeInclusive(	lon,
										-180.0,
										180.0)) {
			return null;
		}

		// Determine the WAC that the coordinate falls in
		r = 21 - (int) Math.round((lat * .25) - .5);
		if (lon < 0.0) {
			lon += 360.0;
		}

		c = (int) lon;

		int wac = lltowac[r][c];

		// If the passed coordinate is an edge case, increment NW by 0.01 degree then
		// recalculate the wac
		final GeodeticPoint[] wacCorners = getWACCorners(wac);

		final double nwLat = Double.parseDouble(f.format(wacCorners[0].latitude()
				.degrees()));
		final double nwLon = Double.parseDouble(f.format(wacCorners[0].longitude()
				.degrees()));
		final double seLat = Double.parseDouble(f.format(wacCorners[1].latitude()
				.degrees()));
		final double seLon = Double.parseDouble(f.format(wacCorners[1].longitude()
				.degrees()));

		boolean recalcLat = false;
		boolean recalcLon = false;

		if ((lat == nwLat) || (lat == seLat)) {
			lat += 0.01;
			r = 21 - (int) Math.round((lat * .25) - .5);
			recalcLat = true;
		}

		// since 'lon' may have been modified,
		// need to go back to the original value
		if ((originalLon == nwLon) || (originalLon == seLon)) {
			lon = originalLon - 0.01;
			if (lon < 0.0) {
				lon += 360.0;
			}

			c = (int) lon;
			recalcLon = true;
		}

		if (recalcLat || recalcLon) {
			wac = lltowac[r][c];
		}

		// Determine the ATC that the coordinate falls in
		/***********************************************************************
		 * NOTE: Do all divisions in hundredths of arc seconds to avoid floating point
		 * errors when coordinates fall directly on a WTM boundaries.
		 */

		deltaLat = wactoll[wac - 1][0] * 360000;
		deltaLat -= (int) (lat * 360000.0);
		deltaLon = (int) (lon * 360000.0);
		deltaLon -= wactoll[wac - 1][1] * 360000;
		if (deltaLon < 0) {
			deltaLon += 129600000;
		}

		final int atc = (((deltaLat / 288000) * 5) + (deltaLon / (int) (getATCWidth(wac) * 360000.0)) + 1);

		// Determine the WTM that the coordinate falls in
		final Point2d[] atcOffsets = getATCOffsets(	wac,
													atc);
		if (atcOffsets != null) {
			deltaLat += (int) (atcOffsets[0].y * 360000.0);
			deltaLon -= (int) (atcOffsets[0].x * 360000.0);
		}
		final int wtm = (short) (((deltaLat / 72000) * 4) + (deltaLon / (int) (getWTMWidth(wac) * 360000.0)) + 1);

		return new WACData(
				wac,
				atc,
				wtm);
	}

	public GeodeticPolygon buildPolyFromId(
			final String wagId ) {
		final WACData wacData = parseWAG(wagId);

		return buildPolyFromId(wacData);
	}

	public GeodeticPolygon buildPolyFromId(
			final WACData wacData ) {
		final GeodeticPoint[] corners = this.getCorners(wacData);
		final List<GeodeticPoint> cornerGeometry = new ArrayList<>();

		if (corners.length == 2) {
			cornerGeometry.add(corners[0]);
			cornerGeometry.add(GeodeticPoint.fromLatLon(corners[0].latitude(),
														corners[1].longitude()));
			cornerGeometry.add(corners[1]);
			cornerGeometry.add(GeodeticPoint.fromLatLon(corners[1].latitude(),
														corners[0].longitude()));
		}
		else {
			// logger.error("Unexpected # of corner points in WACUtils : "
			// + corners.length);
			return null;
		}

		return GeodeticPolygon.create(cornerGeometry);
	}

	public static void main(
			final String[] args ) {

		// Test create WAC,ATC,WTM KML friendly polygon coords
		final WacUtils wacUtils = new WacUtils(
				"/Weather/CloudData3HrSteps/Dec/wacgrid.txt");

		GeodeticPoint[] testPts = wacUtils.getCorners("09000000");
		System.out.println("09000000");
		System.out.println(testPts[0].longitude()
				.degrees() + ","
				+ testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0");

		System.out.println();
		testPts = wacUtils.getCorners("09000100");
		System.out.println("09000100");
		System.out.println(testPts[0].longitude()
				.degrees() + ","
				+ testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0");

		System.out.println();
		testPts = wacUtils.getCorners("09000101");
		System.out.println("09000101");
		System.out.println(testPts[0].longitude()
				.degrees() + ","
				+ testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0");

		System.out.println();
		testPts = wacUtils.getCorners("09000102");
		System.out.println("09000102");
		System.out.println(testPts[0].longitude()
				.degrees() + ","
				+ testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0");

		System.out.println();
		testPts = wacUtils.getCorners("09000103");
		System.out.println("09000103");
		System.out.println(testPts[0].longitude()
				.degrees() + ","
				+ testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0");

		System.out.println();
		testPts = wacUtils.getCorners("09000104");
		System.out.println("09000104");
		System.out.println(testPts[0].longitude()
				.degrees() + ","
				+ testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0");

		System.out.println();
		testPts = wacUtils.getCorners("09000105");
		System.out.println("09000105");
		System.out.println(testPts[0].longitude()
				.degrees() + ","
				+ testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0 " + testPts[1].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[1].latitude()
						.degrees()
				+ ",0 " + testPts[0].longitude()
						.degrees()
				+ "," + testPts[0].latitude()
						.degrees()
				+ ",0");

	}

}
