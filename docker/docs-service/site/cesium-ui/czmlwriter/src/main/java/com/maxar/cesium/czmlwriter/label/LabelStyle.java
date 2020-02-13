package com.maxar.cesium.czmlwriter.label;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.CesiumLabelStyle;
import cesiumlanguagewriter.LabelStyleCesiumWriter;
import cesiumlanguagewriter.Reference;

public interface LabelStyle extends
		Property<LabelStyleCesiumWriter>
{
	public static LabelStyle style(
			final CesiumLabelStyle style ) {
		return writer -> writer.writeLabelStyle(style);
	}

	public static LabelStyle reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}
}
