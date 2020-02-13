package com.maxar.weather.model.map;

import org.locationtech.jts.geom.Geometry;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class WTMModel extends
		MapGridModel
{
	private int wtmId;

	@Builder(builderMethodName = "wtmBuilder")
	public WTMModel(
			final int wtmId,
			final String id,
			final Geometry geometry ) {
		super(
				id,
				geometry);
		this.wtmId = wtmId;
	}
}
