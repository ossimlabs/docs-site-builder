package com.maxar.target.model;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class DsaTargetModel extends
		TargetModel
{
	private double areaSquarekm;
	private TerrainType terrainType;
	
	@Builder(builderMethodName = "dsaBuilder")
	public DsaTargetModel(
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
			double areaSquarekm,
			TerrainType terrainType ) {
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
		this.areaSquarekm = areaSquarekm;
		this.terrainType = terrainType;
	}	
}
