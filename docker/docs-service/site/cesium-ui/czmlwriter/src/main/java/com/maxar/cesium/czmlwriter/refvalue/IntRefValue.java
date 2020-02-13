package com.maxar.cesium.czmlwriter.refvalue;

import java.util.List;

import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.property.deletable.DeletableProperty;
import com.maxar.cesium.czmlwriter.property.interpolation.InterpolatableProperty;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;

import cesiumlanguagewriter.IntegerCesiumWriter;
import cesiumlanguagewriter.Reference;

public interface IntRefValue extends
		InterpolatableProperty<IntegerCesiumWriter, IntRefValue>,
		DeletableProperty<IntegerCesiumWriter, IntRefValue>
{
	public static IntRefValue number(
			final Integer number ) {
		return writer -> writer.writeNumber(number);
	}

	public static IntRefValue number(
			final List<TimeTaggedValue<Integer>> numbers ) {
		return writer -> writer.writeNumber(TimeTaggedValue.listAsJulianDates(numbers),
											TimeTaggedValue.listAsValues(numbers));
	}

	public static IntRefValue reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}

	@Override
	default public IntRefValue wrap(
			final Property<IntegerCesiumWriter> property ) {
		return writer -> property.write(writer);
	}
}
