package com.maxar.target.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.util.GeometricShapeFactory;

import com.maxar.common.utils.GeoUtils;
import com.maxar.target.model.OrderOfBattle;
import com.maxar.target.model.PointTargetModel;
import com.maxar.target.model.TargetModel;
import com.maxar.target.model.TargetType;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "pointtarget")
@Data
@EqualsAndHashCode(callSuper=true)
public class PointTarget extends
		Target
{
	private static final long serialVersionUID = 1L;
	public static double DEFAULT_SIZE_NMI = Length
			.fromKilometers(
					2.0)
			.nmi();
	private static int MAX_POLY_POINTS = 16;

	@Column(name = "majoraxisnmi")
	private double majorAxisNMI;

	@Column(name = "minoraxisnmi")
	private double minorAxisNMI;

	@Column(name = "azimuthdeg")
	private double azimuthDeg;

	@Column(name = "centerlatdeg")
	private double centerLatDeg;

	@Column(name = "centerlondeg")
	private double centerLonDeg;

	@Column(name = "elevationft")
	private double elevationFt;

	protected PointTarget() {
		super(
				TargetType.POINT);
	}

	protected PointTarget(
			final PointTarget pt ) {
		super(
				pt);
		centerLatDeg = pt.centerLatDeg;
		centerLonDeg = pt.centerLonDeg;
		elevationFt = pt.elevationFt;
		majorAxisNMI = pt.majorAxisNMI;
		minorAxisNMI = pt.minorAxisNMI;
		azimuthDeg = pt.azimuthDeg;
	}

	public PointTarget(
			final String targetId,
			final String targetName,
			final double centerLatDeg,
			final double centerLonDeg,
			final double elevationFt ) {
		this(
				targetId,
				targetName,
				null, // description
				centerLatDeg,
				centerLonDeg,
				elevationFt,
				DEFAULT_SIZE_NMI, // majorAxis
				DEFAULT_SIZE_NMI, // minorAxis
				0.0, // azimuth
				"ZZ", // CountryCode
				"ZZ", // GeoRegion
				null); // OorderOfBattle
	}

	public PointTarget(
			final String targetId,
			final String targetName,
			final String description,
			final double centerLatDeg,
			final double centerLonDeg,
			final double elevationFt,
			final double majorAxisNMI,
			final double minorAxisNMI,
			final double azimuthDeg,
			final String countryCode,
			final String geoRegion,
			final OrderOfBattle orderOfBattle ) {
		super(
				TargetType.POINT,
				targetId,
				targetName,
				description,
				countryCode,
				geoRegion,
				orderOfBattle);
		this.centerLatDeg = centerLatDeg;
		this.centerLonDeg = centerLonDeg;
		this.elevationFt = elevationFt;
		this.majorAxisNMI = majorAxisNMI;
		this.minorAxisNMI = minorAxisNMI;
		this.azimuthDeg = azimuthDeg;

		updateGeometry();
	}

	@Override
	public String toString() {
		return String
				.format(
						"POINT: %s %.2f %.2f %.2f %.2f %.2f %.2f",
						targetId,
						majorAxisNMI,
						minorAxisNMI,
						azimuthDeg,
						centerLatDeg,
						centerLonDeg,
						elevationFt);
	}

	private void updateGeometry() {
		// create polygon from ellipse

		// make sure all pieces are in place first
		final GeometricShapeFactory gsf = new GeometricShapeFactory();

		gsf
				.setCentre(
						new Coordinate(
								Angle
										.fromDegrees(
												centerLonDeg)
										.degrees(),
								Angle
										.fromDegrees(
												centerLatDeg)
										.degrees()));
		gsf
				.setHeight(
						GeoUtils
								.northSouthDistanceAsAngle(
										Length
												.fromNmi(
														majorAxisNMI))
								.degrees());
		gsf
				.setWidth(
						GeoUtils
								.eastWestDistanceAsAngleAtLatitude(
										Angle
												.fromDegrees(
														centerLatDeg),
										Length
												.fromNmi(
														minorAxisNMI))
								.degrees());
		gsf
				.setRotation(
						Angle
								.fromDegrees(
										azimuthDeg)
								.negate()
								.degrees());
		gsf
				.setNumPoints(
						MAX_POLY_POINTS); // TODO: make a parameter?
		geometry = gsf.createEllipse();
	}
	
	@Override
	public TargetModel toModel(DateTime czmlStartTime, DateTime czmlStopTime) {
		return PointTargetModel.pointBuilder()
				.targetType(targetType)
				.targetId(targetId)
				.targetName(targetName)
				.description(description)
				.countryCode(countryCode)
				.geoRegion(geoRegion)
				.orderOfBattle(orderOfBattle)
				.geometry(geometry)
				.majorAxisNMI(majorAxisNMI)
				.minorAxisNMI(minorAxisNMI)
				.azimuthDeg(azimuthDeg)
				.centerLatDeg(centerLatDeg)
				.centerLonDeg(centerLonDeg)
				.elevationFt(elevationFt)
				.czmlStartTime(czmlStartTime)
				.czmlStopTime(czmlStopTime)
				.build();
	}
}
