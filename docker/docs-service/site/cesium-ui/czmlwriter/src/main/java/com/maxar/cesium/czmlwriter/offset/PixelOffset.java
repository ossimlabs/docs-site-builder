package com.maxar.cesium.czmlwriter.offset;

import java.util.List;

import com.maxar.cesium.czmlwriter.CesiumLanguageWriterUtils;
import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.radiantblue.analytics.core.Vector2D;

import cesiumlanguagewriter.PixelOffsetCesiumWriter;
import cesiumlanguagewriter.Reference;

public interface PixelOffset extends
		Property<PixelOffsetCesiumWriter>
{
	public static PixelOffset cartesian2(
			final double x,
			final double y ) {
		return writer -> writer.writeCartesian2(x,
												y);
	}

	public static PixelOffset cartesian2(
			final List<TimeTaggedValue<Vector2D>> padding ) {
		return writer -> writer.writeCartesian2(TimeTaggedValue.listAsJulianDates(padding),
												TimeTaggedValue.listAsValues(	padding,
																				CesiumLanguageWriterUtils::cartesian2));
	}

	public static PixelOffset reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}
}
