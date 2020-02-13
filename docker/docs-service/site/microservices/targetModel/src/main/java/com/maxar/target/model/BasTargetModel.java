package com.maxar.target.model;

import java.util.Set;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class BasTargetModel extends
		TargetModel
{
	private Set<BasWtmModel> wtms;

	@Builder(builderMethodName = "basBuilder")
	public BasTargetModel(
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
			Set<BasWtmModel> wtms ) {
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
		this.wtms = wtms;
	}
}
