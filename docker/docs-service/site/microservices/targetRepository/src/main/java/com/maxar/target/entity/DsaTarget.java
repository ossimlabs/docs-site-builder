package com.maxar.target.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

import com.maxar.common.utils.GeoUtils;
import com.maxar.target.model.DsaTargetModel;
import com.maxar.target.model.TargetModel;
import com.maxar.target.model.TargetType;
import com.maxar.target.model.TerrainType;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;
import com.radiantblue.analytics.geodesy.geometry.GeodeticPolygon;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "dsatarget")
@Data
@EqualsAndHashCode(callSuper=true)
public class DsaTarget extends
		Target
{
	private static final long serialVersionUID = 1L;

	@Column(name = "areasquarekm")
	private double areaSquarekm;

	@Column(name = "terraintype")
	@Enumerated
	private TerrainType terrainType;

	public DsaTarget() {
		super(
				TargetType.DSA);
	}

	public DsaTarget(
			final DsaTarget dsa ) {
		super(
				dsa);
		areaSquarekm = dsa.areaSquarekm;
		terrainType = dsa.terrainType;
	}

	public DsaTarget(
			final String targetId,
			final String targetName,
			final Geometry geom ) {
		super(
				TargetType.DSA,
				targetId,
				targetName,
				null, // description
				"ZZ", // country code
				"ZZ", // GeoRegion
				null, // order of battle
				geom);
		updateGeometry();
	}

	public void setDsaGeometry(
			final List<GeodeticPoint> points ) {
		geometry = GeodeticPolygon
				.create(
						points)
				.jtsGeometry_deg();
		updateGeometry();
	}

	public void setDsaGeometry(
			final GeodeticPolygon poly ) {
		geometry = poly.jtsGeometry_deg();
		updateGeometry();
	}

	@Override
	public String toString() {
		return String
				.format(
						"DSA: %6s %.2f %s",
						targetId,
						areaSquarekm,
						targetName);
	}

	protected void updateGeometry() {
		// this give bad results if degrees,so convert to radians
		areaSquarekm = GeodeticGeometry
				.create(
						GeoUtils.convertDegreesToRadians(geometry))
				.area()
				.kmSquared();
	}
	
	@Override
	public TargetModel toModel(DateTime czmlStartTime, DateTime czmlStopTime) {
		return DsaTargetModel.dsaBuilder()
				.targetType(targetType)
				.targetId(targetId)
				.targetName(targetName)
				.description(description)
				.countryCode(countryCode)
				.geoRegion(geoRegion)
				.orderOfBattle(orderOfBattle)
				.geometry(geometry)
				.areaSquarekm(areaSquarekm)
				.terrainType(terrainType)
				.czmlStartTime(czmlStartTime)
				.czmlStopTime(czmlStopTime)
				.build();
	}
}
