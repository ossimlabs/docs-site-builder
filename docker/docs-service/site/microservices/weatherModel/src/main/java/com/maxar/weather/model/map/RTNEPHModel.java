package com.maxar.weather.model.map;

import org.locationtech.jts.geom.Geometry;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class RTNEPHModel extends
		MapGridModel
{
	private int rtnephId;

	private boolean northernHemisphere;

	@Builder(builderMethodName = "rtnephBuilder")
	public RTNEPHModel(
			final int rtnephId,
			final boolean northernHemisphere,
			final String id,
			final Geometry geometry ) {
		super(
				id,
				geometry);
		this.rtnephId = rtnephId;
		this.northernHemisphere = northernHemisphere;
	}
}
