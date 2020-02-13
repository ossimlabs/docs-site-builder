package com.maxar.target.model;

import java.util.Set;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class LocTargetModel extends TargetModel
{
	private Set<LocSegmentTargetModel> segments;
	
	@Builder(builderMethodName = "locBuilder")
	public LocTargetModel(
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
			final Set<LocSegmentTargetModel> segments) {
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
		this.segments = segments;
	}
}
