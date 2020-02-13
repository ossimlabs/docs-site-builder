package com.maxar.cesium.czmlwriter.refvalue;

import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.property.deletable.DeletableProperty;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.Reference;

public interface BooleanRefValue extends
		Property<BooleanCesiumWriter>,
		DeletableProperty<BooleanCesiumWriter, BooleanRefValue>
{
	public static BooleanRefValue booleanValue(
			final Boolean booleanVal ) {
		return writer -> writer.writeBoolean(booleanVal);
	}

	public static BooleanRefValue reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}

	@Override
	default public BooleanRefValue wrap(
			final Property<BooleanCesiumWriter> property ) {
		return writer -> property.write(writer);
	}
}