package com.maxar.cesium.czmlwriter.refvalue;

import java.awt.Color;
import java.util.List;

import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.property.deletable.DeletableProperty;
import com.maxar.cesium.czmlwriter.property.interpolation.InterpolatableProperty;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;

import cesiumlanguagewriter.ColorCesiumWriter;
import cesiumlanguagewriter.Reference;

public interface ColorRefValue extends
		InterpolatableProperty<ColorCesiumWriter, ColorRefValue>,
		DeletableProperty<ColorCesiumWriter, ColorRefValue>
{
	public static ColorRefValue color(
			final Color color ) {
		return writer -> writer.writeRgba(color);
	}

	public static ColorRefValue number(
			final List<TimeTaggedValue<Color>> colors ) {
		return writer -> writer.writeRgba(	TimeTaggedValue.listAsJulianDates(colors),
											TimeTaggedValue.listAsValues(colors));
	}

	public static ColorRefValue reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}

	@Override
	default public ColorRefValue wrap(
			final Property<ColorCesiumWriter> property ) {
		return writer -> property.write(writer);
	}
}