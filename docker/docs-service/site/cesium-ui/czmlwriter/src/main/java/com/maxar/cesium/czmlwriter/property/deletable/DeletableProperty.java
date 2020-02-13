package com.maxar.cesium.czmlwriter.property.deletable;

import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.property.PropertyMixin;

import cesiumlanguagewriter.advanced.ICesiumDeletablePropertyWriter;

public interface DeletableProperty<A extends ICesiumDeletablePropertyWriter, B extends DeletableProperty<A, B>> extends
		PropertyMixin<A, B>
{

	public static <A extends ICesiumDeletablePropertyWriter> Property<A> createDelete() {
		return writer -> {
			writer.writeDelete(true);
		};
	}

	default public B delete() {
		return delete(true);
	}

	default public B delete(
			final boolean delete ) {
		return wrap(writer -> {
			writer.writeDelete(delete);
			write(writer);
		});
	}
}
