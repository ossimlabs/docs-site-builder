package com.maxar.cesium.czmlwriter.types;

import com.maxar.cesium.czmlwriter.CesiumLanguageWriterUtils;
import com.radiantblue.analytics.core.Vector3D;

import cesiumlanguagewriter.Cartesian;
import cesiumlanguagewriter.Motion1;
import lombok.Data;

@Data
public class PositionVelocity
{
	final Vector3D position;
	final Vector3D velocity;

	public Motion1<Cartesian> asMotion1() {
		return new Motion1<>(
				new Cartesian[] {
					CesiumLanguageWriterUtils.cartesian(position),
					CesiumLanguageWriterUtils.cartesian(velocity)
				});
	}
}
