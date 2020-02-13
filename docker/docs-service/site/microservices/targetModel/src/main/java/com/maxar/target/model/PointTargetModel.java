package com.maxar.target.model;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.util.GeometricShapeFactory;

import com.maxar.common.utils.GeoUtils;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PointTargetModel extends
		TargetModel
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	private double majorAxisNMI;
	private double minorAxisNMI;
	private double azimuthDeg;
	private double centerLatDeg;
	private double centerLonDeg;
	private double elevationFt;

	@Builder(builderMethodName = "pointBuilder")
	public PointTargetModel(
			TargetType targetType,
			String targetId,
			String targetName,
			String description,
			String countryCode,
			String geoRegion,
			boolean estimated,
			DateTime czmlStartTime,
			DateTime czmlStopTime,
			OrderOfBattle orderOfBattle,
			Geometry geometry,
			double majorAxisNMI,
			double minorAxisNMI,
			double azimuthDeg,
			double centerLatDeg,
			double centerLonDeg,
			double elevationFt ) {
		super(
				targetType,
				targetId,
				targetName,
				description,
				countryCode,
				geoRegion,
				estimated,
				czmlStartTime,
				czmlStopTime,
				orderOfBattle,
				geometry);
		this.majorAxisNMI = majorAxisNMI;
		this.minorAxisNMI = minorAxisNMI;
		this.azimuthDeg = azimuthDeg;
		this.centerLatDeg = centerLatDeg;
		this.centerLonDeg = centerLonDeg;
		this.elevationFt = elevationFt;
	}

	public static PointTargetModel generateEstimatedTarget(
			String geometryWKT,
			String targetId,
			String targetName,
			double minorAxisMeters,
			double majorAxisMeters,
			int numPolygonPoints,
			DateTime czmlStartTime,
			DateTime czmlStopTime) {
		final WKTReader reader = new WKTReader();
		Geometry requestedGeometry = null;
		try {
			requestedGeometry = reader.read(geometryWKT);
		}
		catch (final ParseException e) {
			logger.error("Cannot parse WKT string: " + geometryWKT);
			return null;
		}
		// Need to get center point from geom
		Coordinate center = requestedGeometry.getCentroid()
				.getCoordinate();

		final GeometricShapeFactory gsf = new GeometricShapeFactory();

		gsf.setCentre(center);
		gsf.setHeight(GeoUtils.northSouthDistanceAsAngle(Length.fromMeters(majorAxisMeters))
				.degrees());
		gsf.setWidth(GeoUtils.eastWestDistanceAsAngleAtLatitude(Angle.fromDegrees(center.getY()),
																Length.fromMeters(minorAxisMeters))
				.degrees());
		gsf.setRotation(Angle.fromDegrees(0.0)
				.negate()
				.degrees());
		gsf.setNumPoints(numPolygonPoints);
		Geometry geometry = gsf.createEllipse();

		return PointTargetModel.pointBuilder()
				.targetType(TargetType.POINT)
				.targetId(targetId)
				.targetName(targetName)
				.centerLatDeg(center.getY())
				.centerLonDeg(center.getX())
				.azimuthDeg(0.0)
				.minorAxisNMI(Length.fromMeters(minorAxisMeters)
						.nmi())
				.majorAxisNMI(Length.fromMeters(majorAxisMeters)
						.nmi())
				.geometry(geometry)
				.estimated(true)
				.czmlStartTime(czmlStartTime)
				.czmlStopTime(czmlStopTime)
				.build();
	}
}
