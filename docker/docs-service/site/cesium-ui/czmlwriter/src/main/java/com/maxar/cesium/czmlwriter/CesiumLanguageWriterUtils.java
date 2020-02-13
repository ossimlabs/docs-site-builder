package com.maxar.cesium.czmlwriter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.locationtech.jts.geom.Coordinate;

import com.radiantblue.analytics.core.Quaternion;
import com.radiantblue.analytics.core.Vector2D;
import com.radiantblue.analytics.core.Vector3D;
import com.radiantblue.analytics.geodesy.EarthCenteredFrame;
import com.radiantblue.analytics.geodesy.LatLonAlt;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.Cartographic;
import cesiumlanguagewriter.JulianDate;
import cesiumlanguagewriter.Rectangular;
import cesiumlanguagewriter.UnitQuaternion;

public class CesiumLanguageWriterUtils
{
	private final static String REFERENCE_FRAME_FIXED = "FIXED";
	private final static String REFERENCE_FRAME_INERTIAL = "INERTIAL";

	private final static Map<EarthCenteredFrame, String> CESIUM_REFERENCE_FRAME = new HashMap<>() {
		private static final long serialVersionUID = 1L;
		{
			put(EarthCenteredFrame.ECEF,
				REFERENCE_FRAME_FIXED);
			put(EarthCenteredFrame.ECI_J2000,
				REFERENCE_FRAME_INERTIAL);
			put(EarthCenteredFrame.ECI_TEME,
				REFERENCE_FRAME_INERTIAL);
			put(EarthCenteredFrame.GCRF,
				REFERENCE_FRAME_INERTIAL);
			put(EarthCenteredFrame.TrueOfDate,
				REFERENCE_FRAME_INERTIAL);
		}
	};

	public static JulianDate joda2Julian(
			final DateTime dt ) {

		final Instant instant = Instant.ofEpochMilli(dt.getMillis());
		final ZoneId zoneId = ZoneId.of(dt.getZone()
				.getID(),
										ZoneId.SHORT_IDS);
		final ZonedDateTime zdt = ZonedDateTime.ofInstant(	instant,
															zoneId);

		return new JulianDate(
				zdt);
	}

	public static cesiumlanguagewriter.Duration joda2Duration(
			final Duration duration ) {
		return new cesiumlanguagewriter.Duration(
				(int) duration.getStandardDays(),
				(int) duration.getStandardHours(),
				(int) duration.getStandardMinutes(),
				(int) duration.getStandardSeconds());
	}

	public static UnitQuaternion unitQuaternion(
			final Quaternion quaternion ) {
		return new UnitQuaternion(
				quaternion.w(),
				quaternion.x(),
				quaternion.y(),
				quaternion.z());
	}

	public static String referenceFrame(
			final EarthCenteredFrame frame ) {
		return CESIUM_REFERENCE_FRAME.get(frame);
	}

	public static Cartesian cartesian(
			final Vector3D vector3D ) {
		return new Cartesian(
				vector3D.x(),
				vector3D.y(),
				vector3D.z());
	}

	public static Rectangular cartesian2(
			final Vector2D vector2D ) {
		return new Rectangular(
				vector2D.x(),
				vector2D.y());
	}

	public static Cartographic cartographicDegrees(
			final LatLonAlt latLonAlt ) {
		return new Cartographic(
				latLonAlt.longitude()
						.degrees(),
				latLonAlt.latitude()
						.degrees(),
				latLonAlt.altitude()
						.meters());
	}

	public static Cartographic cartographicRadians(
			final LatLonAlt latLonAlt ) {
		return new Cartographic(
				latLonAlt.longitude()
						.radians(),
				latLonAlt.latitude()
						.radians(),
				latLonAlt.altitude()
						.meters());
	}

	public static Cartographic coordinate(
			final Coordinate coordinate ) {

		final double z = Double.isFinite(coordinate.getZ()) ? coordinate.getZ() : 0.0;

		return new Cartographic(
				coordinate.getX(),
				coordinate.getY(),
				z);
	}
}
