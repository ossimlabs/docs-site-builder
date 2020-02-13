package com.maxar.cesium.czmlwriter.property;

import cesiumlanguagewriter.advanced.ICesiumPropertyWriter;

public interface PropertyMixin<A extends ICesiumPropertyWriter, B extends PropertyMixin<A, B>> extends
		Property<A>
{
	public B wrap(
			Property<A> propertyWriter );
}
