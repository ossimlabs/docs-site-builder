package com.maxar.cesium.czmlwriter.label;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.FontCesiumWriter;
import cesiumlanguagewriter.Reference;

public interface Font extends
		Property<FontCesiumWriter>
{
	public static Font font(
			final String string ) {
		return writer -> writer.writeFont(string);

	}

	public static Font reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}
}
