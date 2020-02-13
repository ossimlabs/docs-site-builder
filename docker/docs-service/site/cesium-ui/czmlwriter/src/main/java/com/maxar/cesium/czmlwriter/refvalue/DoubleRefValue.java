package com.maxar.cesium.czmlwriter.refvalue;

import java.util.List;

import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.property.deletable.DeletableProperty;
import com.maxar.cesium.czmlwriter.property.interpolation.InterpolatableProperty;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;

import cesiumlanguagewriter.DoubleCesiumWriter;
import cesiumlanguagewriter.Reference;

public interface DoubleRefValue extends
		InterpolatableProperty<DoubleCesiumWriter, DoubleRefValue>,
		DeletableProperty<DoubleCesiumWriter, DoubleRefValue>
{
	public static DoubleRefValue number(
			final Double number ) {
		return writer -> writer.writeNumber(number);
	}

	public static DoubleRefValue number(
			final List<TimeTaggedValue<Double>> numbers ) {
		return writer -> writer.writeNumber(TimeTaggedValue.listAsJulianDates(numbers),
											TimeTaggedValue.listAsValues(numbers));
	}

	public static DoubleRefValue reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}

	@Override
	default public DoubleRefValue wrap(
			final Property<DoubleCesiumWriter> property ) {
		return writer -> property.write(writer);
	}
}
