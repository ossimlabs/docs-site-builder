package com.maxar.cesium.czmlwriter.material;

import static com.maxar.cesium.czmlwriter.property.Property.writeAndClose;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;

public interface Material extends
		Property<MaterialCesiumWriter>
{
	public static Material create() {
		return writer -> {};
	}

	default Material solidColor(
			final Property<SolidColorMaterialCesiumWriter> solidColorMaterial ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openSolidColorProperty(),
							solidColorMaterial);
		};
	}

	// image
	// grid
	// stripe
	// checkerboard
}
