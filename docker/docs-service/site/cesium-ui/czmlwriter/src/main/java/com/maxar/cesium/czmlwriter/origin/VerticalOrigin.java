package com.maxar.cesium.czmlwriter.origin;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.CesiumVerticalOrigin;
import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.VerticalOriginCesiumWriter;

public interface VerticalOrigin extends
		Property<VerticalOriginCesiumWriter>
{
	public static VerticalOrigin verticalOrigin(
			final CesiumVerticalOrigin origin ) {
		return writer -> writer.writeVerticalOrigin(origin);
	}

	public static VerticalOrigin reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}
}
