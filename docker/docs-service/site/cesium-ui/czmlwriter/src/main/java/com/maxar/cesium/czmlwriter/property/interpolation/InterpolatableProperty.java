package com.maxar.cesium.czmlwriter.property.interpolation;

import com.maxar.cesium.czmlwriter.property.PropertyMixin;

import cesiumlanguagewriter.advanced.CesiumInterpolatablePropertyWriter;

public interface InterpolatableProperty<A extends CesiumInterpolatablePropertyWriter<A>, B extends InterpolatableProperty<A, B>>
		extends
		PropertyMixin<A, B>
{
	default public B interpolated(
			final InterpolationProperties<A> interpProps ) {
		return wrap(writer -> {
			interpProps.write(writer);
			write(writer);
		});
	}
}
