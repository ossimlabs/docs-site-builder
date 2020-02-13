package com.maxar.cesium.czmlwriter.material;

import static com.maxar.cesium.czmlwriter.property.Property.writeAndClose;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.ColorCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;

public interface SolidColorMaterial extends
	Property<SolidColorMaterialCesiumWriter>
{
	public static SolidColorMaterial create() {
		return writer -> {};
	}
	
	default SolidColorMaterial solidColor(
			final Property<ColorCesiumWriter> color ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openColorProperty(),
							color);
		};
	}
}
