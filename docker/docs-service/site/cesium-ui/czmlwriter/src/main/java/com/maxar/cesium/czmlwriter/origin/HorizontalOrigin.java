package com.maxar.cesium.czmlwriter.origin;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.CesiumHorizontalOrigin;
import cesiumlanguagewriter.HorizontalOriginCesiumWriter;
import cesiumlanguagewriter.Reference;

public interface HorizontalOrigin extends
		Property<HorizontalOriginCesiumWriter>
{
	public static HorizontalOrigin horizontalOrigin(
			final CesiumHorizontalOrigin origin ) {
		return writer -> writer.writeHorizontalOrigin(origin);
	}

	public static HorizontalOrigin reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}
}
