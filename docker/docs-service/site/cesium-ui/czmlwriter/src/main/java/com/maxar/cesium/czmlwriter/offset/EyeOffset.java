package com.maxar.cesium.czmlwriter.offset;

import java.util.List;

import com.maxar.cesium.czmlwriter.CesiumLanguageWriterUtils;
import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.radiantblue.analytics.core.Vector3D;

import cesiumlanguagewriter.EyeOffsetCesiumWriter;
import cesiumlanguagewriter.Reference;

public interface EyeOffset extends
		Property<EyeOffsetCesiumWriter>
{
	public static EyeOffset cartesian(
			final Vector3D offset ) {
		return writer -> writer.writeCartesian(CesiumLanguageWriterUtils.cartesian(offset));
	}

	public static EyeOffset cartesian(
			final List<TimeTaggedValue<Vector3D>> offset ) {
		return writer -> writer.writeCartesian(	TimeTaggedValue.listAsJulianDates(offset),
												TimeTaggedValue.listAsValues(	offset,
																				CesiumLanguageWriterUtils::cartesian));
	}

	public static EyeOffset reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}
}
