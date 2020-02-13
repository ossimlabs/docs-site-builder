package com.maxar.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.GeometryEditor;

import com.radiantblue.analytics.core.Vector3D;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;
import com.radiantblue.analytics.geodesy.geometry.GeodeticLineString;
import com.radiantblue.analytics.geodesy.geometry.GeodeticMultiPolygon;
import com.radiantblue.analytics.geodesy.geometry.GeodeticPolygon;

public class GeoUtils
{
	private static Logger logger = SourceLogger
			.getLogger(
					new Object() {}.getClass().getEnclosingClass().getName());

	public static final double RE_KM = 6378.137;
	public static final double CE_KM = 2.0 * Math.PI * RE_KM;
	public static final double DEG_KM = CE_KM / 360.0;

	public static double KMtoNMI(
			final double km ) {
		return km / 1.852;
	}

	public static double KMtoFT(
			final double km ) {
		return km * 3281.0;
	}

	public static double KM_AT_LAT(
			final double lat ) {
		return Math
				.cos(
						Math
								.toRadians(
										lat))
				* DEG_KM;
	}

	/**
	 * approximation of distance between degrees at a particular latitude For use
	 * with "radius" searches
	 *
	 * @param lat
	 * @param dist
	 * @return Degrees length dist equals at latitude lat along a line of longitude
	 */
	public static Angle eastWestDistanceAsAngleAtLatitude(
			final Angle lat,
			final Length dist ) {
		return Angle
				.fromDegrees(
						dist.km() / KM_AT_LAT(
								lat.degrees()));
	}

	public static Angle northSouthDistanceAsAngle(
			final Length dist ) {
		return Angle
				.fromDegrees(
						dist.km() / DEG_KM);
	}

	public static GeodeticPoint[] coordinatesToGeodeticPoints(
			final Coordinate[] coords ) {
		final GeodeticPoint[] gps = new GeodeticPoint[coords.length];

		int i = 0;
		for (final Coordinate coord : coords) {
			gps[i++] = coordinateToGeodeticPoint(
					coord);
		}

		return gps;
	}

	public static GeodeticPoint coordinateToGeodeticPoint(
			final Coordinate coord ) {
		return GeodeticPoint
				.fromLatLon(
						Angle
								.fromRadians(
										coord.y),
						Angle
								.fromRadians(
										coord.x));
	}

	public static Coordinate geodeticPointToCoordinate(
			final GeodeticPoint gp ) {

		return (new Coordinate(
				gp.longitude().radians(),
				gp.latitude().radians()));
	}

	public static Coordinate[] geodeticPointsToCoordinates(
			final Collection<GeodeticPoint> gps ) {

		if ((gps == null) || (gps.size() == 0)) {
			logger
					.debug(
							"### No GPS");
			return null;
		}
		final Coordinate[] coords = new Coordinate[gps.size()];

		int i = 0;
		for (final GeodeticPoint gp : gps) {
			coords[i++] = geodeticPointToCoordinate(
					gp);
		}

		return coords;
	}

	public static GeodeticPoint positionFromAzimuthAndDistance(
			final GeodeticPoint center,
			final Angle azimuth,
			final Length distance ) {

		final Vector3D vDir = center
				.getDirectionAtAzimuth(
						azimuth)
				.unit();
		final Vector3D vCenter = center.ecfPosition();

		// calculate position, altitude will be great if distance is large
		final Vector3D vPos = vCenter
				.addAndScale(
						distance.meters(),
						vDir);

		return GeodeticPoint
				.fromEcf(
						vPos);
	}

	public static List<GeodeticPoint> frameFromDistance(
			final GeodeticPoint center,
			final Length distance ) {

		final List<GeodeticPoint> frame = new ArrayList<>();

		// get extents
		final GeodeticPoint north = positionFromAzimuthAndDistance(
				center,
				Angle
						.fromDegrees(
								0),
				distance);
		final GeodeticPoint south = positionFromAzimuthAndDistance(
				center,
				Angle
						.fromDegrees(
								180),
				distance);
		final GeodeticPoint east = positionFromAzimuthAndDistance(
				center,
				Angle
						.fromDegrees(
								90),
				distance);
		final GeodeticPoint west = positionFromAzimuthAndDistance(
				center,
				Angle
						.fromDegrees(
								270),
				distance);

		// corners, starting with lower left going clock-wise
		frame
				.add(
						GeodeticPoint
								.fromLatLon(
										south.latitude(),
										west.longitude()));
		frame
				.add(
						GeodeticPoint
								.fromLatLon(
										north.latitude(),
										west.longitude()));
		frame
				.add(
						GeodeticPoint
								.fromLatLon(
										north.latitude(),
										east.longitude()));
		frame
				.add(
						GeodeticPoint
								.fromLatLon(
										south.latitude(),
										east.longitude()));

		return frame;
	}

	public static Length computeDistance(
			final GeodeticPoint point1,
			final GeodeticPoint point2 ) {
		final Vector3D v1 = point1.ecfPosition();
		final Vector3D v2 = point2.ecfPosition();

		final Vector3D diff = v2
				.plus(
						v1.negate());
		return Length
				.fromMeters(
						diff.length());
	}

	public static GeodeticPoint[] getGeodeticPoints(
			final GeodeticGeometry geodeticGeometry ) {

		if (geodeticGeometry == null) {
			return null;
		}

		GeodeticPoint[] points = null;

		if (geodeticGeometry instanceof GeodeticPolygon) {

			final GeodeticPolygon poly = (GeodeticPolygon) geodeticGeometry;

			points = poly.shell();
		}
		else if (geodeticGeometry instanceof GeodeticLineString) {

			// check underlying JTS geometry
			final GeodeticLineString line = (GeodeticLineString) geodeticGeometry;

			points = line.points();
		}
		else if (geodeticGeometry instanceof GeodeticMultiPolygon) {

			final GeodeticMultiPolygon gmp = (GeodeticMultiPolygon) geodeticGeometry;

			// Should look at individual polys??
			points = gmp.envelope().shell();
		}
		else {
			logger
					.warn(
							"Unknown GeodeticGeometry: " + geodeticGeometry.getClass().getName());
		}

		return points;
	}

	public static List<GeodeticPoint> avoidPoles(
			final GeodeticPoint point,
			final Angle lastLongitude,
			final Angle nextLongitude ) {

		final double deltaDegrees = 1.0E-2;

		double latDegrees = point.latitude().degrees();

		final List<GeodeticPoint> gps = new ArrayList<>();

		if (Math
				.abs(
						latDegrees) == 90.0) {

			if (latDegrees < 0) {
				latDegrees += deltaDegrees;
			}
			else {
				latDegrees -= deltaDegrees;
			}

			gps
					.add(
							GeodeticPoint
									.fromLatLonAlt(
											Angle
													.fromDegrees(
															latDegrees),
											lastLongitude,
											point.altitude()));

			gps
					.add(
							GeodeticPoint
									.fromLatLonAlt(
											Angle
													.fromDegrees(
															latDegrees),
											nextLongitude,
											point.altitude()));

		}
		else {
			gps
					.add(
							point);
		}

		return gps;
	}

	public static GeodeticPolygon GeodeticPolygonConvexHull(
			final GeodeticPolygon inPoly ) {

		final Geometry hull = inPoly.jtsGeometry().convexHull();

		final GeodeticPolygon poly = (GeodeticPolygon) GeodeticGeometry
				.create(
						hull);

		return poly;
	}

	public static Angle latitudeFromDMS(
			final String inHDMS ) {
		// Bluesim puts hemisphere as first character
		int hemMultiplier = 1;

		if (inHDMS
				.toUpperCase()
				.contains(
						"S")) {
			hemMultiplier = -1;
		}

		int posOfHem = inHDMS
				.toUpperCase()
				.indexOf(
						'S');
		if (posOfHem == -1) {
			posOfHem = inHDMS
					.toUpperCase()
					.indexOf(
							'N');
		}

		// just DDMMSS
		String trimmedInHDMS = "";
		if (posOfHem == 0) {
			trimmedInHDMS = inHDMS
					.substring(
							1);
		}
		else {
			trimmedInHDMS = inHDMS
					.substring(
							0,
							inHDMS.length() - 1);
		}

		final int degrees = Integer
				.parseInt(
						trimmedInHDMS
								.substring(
										0,
										2));
		final int minutes = Integer
				.parseInt(
						trimmedInHDMS
								.substring(
										2,
										4));
		final int seconds = Integer
				.parseInt(
						trimmedInHDMS
								.substring(
										4));

		final double decDegrees = degrees + (minutes / 60.0) + (seconds / 3600.0);

		return Angle
				.fromDegrees(
						hemMultiplier * decDegrees);

	}

	public static Angle longitudeFromDMS(
			final String inHDMS ) {
		// Bluesim puts hemisphere as first character
		int hemMultiplier = 1;

		if (inHDMS
				.toUpperCase()
				.contains(
						"W")) {
			hemMultiplier = -1;
		}

		int posOfHem = inHDMS
				.toUpperCase()
				.indexOf(
						'E');
		if (posOfHem == -1) {
			posOfHem = inHDMS
					.toUpperCase()
					.indexOf(
							'W');
		}

		// just DDMMSS
		String trimmedInHDMS = "";
		if (posOfHem == 0) {
			trimmedInHDMS = inHDMS
					.substring(
							1);
		}
		else {
			trimmedInHDMS = inHDMS
					.substring(
							0,
							inHDMS.length() - 1);
		}

		final int degrees = Integer
				.parseInt(
						trimmedInHDMS
								.substring(
										0,
										3));
		final int minutes = Integer
				.parseInt(
						trimmedInHDMS
								.substring(
										3,
										5));
		final int seconds = Integer
				.parseInt(
						trimmedInHDMS
								.substring(
										5));

		final double decDegrees = degrees + (minutes / 60.0) + (seconds / 3600.0);

		return Angle
				.fromDegrees(
						hemMultiplier * decDegrees);

	}
	
	public static Geometry convertRadiansToDegrees(
			final Geometry geometry ) {
		final GeometryEditor editor = new GeometryEditor();
		final Geometry newGeom = editor
				.edit(
						geometry,
						new GeometryEditor.CoordinateOperation() {

							@Override
							public Coordinate[] edit(
									final Coordinate[] coords,
									final Geometry arg1 ) {
								final Coordinate[] newCoords = new Coordinate[coords.length];
								for (int i = 0; i < coords.length; i++) {
									final double newX = (coords[i].getX() * 180.0) / Math.PI;
									final double newY = (coords[i].getY() * 180.0) / Math.PI;
									newCoords[i] = new Coordinate(
											newX,
											newY,
											coords[i].getZ());
								}
								return newCoords;
							}

						});

		return newGeom;
	}

	public static Geometry convertDegreesToRadians(
			final Geometry geometry ) {
		final GeometryEditor editor = new GeometryEditor();
		final Geometry newGeom = editor
				.edit(
						geometry,
						new GeometryEditor.CoordinateOperation() {

							@Override
							public Coordinate[] edit(
									final Coordinate[] coords,
									final Geometry arg1 ) {
								final Coordinate[] newCoords = new Coordinate[coords.length];
								for (int i = 0; i < coords.length; i++) {
									final double newX = (coords[i].getX() / 180.0) * Math.PI;
									final double newY = (coords[i].getY() / 180.0) * Math.PI;
									newCoords[i] = new Coordinate(
											newX,
											newY,
											coords[i].getZ());
								}
								return newCoords;
							}

						});

		return newGeom;
	}
}
