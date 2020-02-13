package com.maxar.cesium.czmlwriter.refvalue;

import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.property.deletable.DeletableProperty;

import cesiumlanguagewriter.Reference;
import cesiumlanguagewriter.StringCesiumWriter;

public interface StringRefValue extends
		Property<StringCesiumWriter>,
		DeletableProperty<StringCesiumWriter, StringRefValue>
{
	public static StringRefValue string(
			final String string ) {
		return writer -> writer.writeString(string);
	}

	public static StringRefValue reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}

	@Override
	default public StringRefValue wrap(
			final Property<StringCesiumWriter> property ) {
		return writer -> property.write(writer);
	}
}