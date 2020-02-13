package com.maxar.target.model;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class LocSegmentTargetModel extends
		TargetModel
{
	protected int num;
	protected double startLatDeg;
	protected double startLonDeg;
	protected double startElevationFt;
	protected double endLatDeg;
	protected double endLonDeg;
	protected double endElevationFt;
	
	@Builder(builderMethodName = "locSegmentBuilder")
	public LocSegmentTargetModel(
			final TargetType targetType,
			final String targetId,
			final String targetName,
			final String description,
			final String countryCode,
			final String geoRegion,
			final boolean estimated,
			DateTime czmlStartTime,
			DateTime czmlStopTime,
			final OrderOfBattle orderOfBattle,
			final Geometry geometry,
			final int num,
			final double startLatDeg,
			final double startLonDeg,
			final double startElevationFt,
			final double endLatDeg,
			final double endLonDeg,
			final double endElevationFt) {
		super(targetType,
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
		this.num = num;
		this.startLatDeg = startLatDeg;
		this.startLonDeg = startLonDeg;
		this.startElevationFt = startElevationFt;
		this.endLatDeg = endLatDeg;
		this.endLonDeg = endLonDeg;
		this.endElevationFt = endElevationFt;
	}
}
