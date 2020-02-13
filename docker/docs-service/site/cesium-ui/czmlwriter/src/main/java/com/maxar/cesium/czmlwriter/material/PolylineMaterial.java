package com.maxar.cesium.czmlwriter.material;

import static com.maxar.cesium.czmlwriter.property.Property.writeAndClose;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.PolylineMaterialCesiumWriter;
import cesiumlanguagewriter.PolylineOutlineMaterialCesiumWriter;

public interface PolylineMaterial extends
		Property<PolylineMaterialCesiumWriter>
{
	public static PolylineMaterial create() {
		return writer -> {};
	}

	// TODO solidColor

	default PolylineMaterial polylineOutline(
			final Property<PolylineOutlineMaterialCesiumWriter> polylineOutlineMaterial ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPolylineOutlineProperty(),
							polylineOutlineMaterial);
		};
	}

	// TODO polylineArrow
	// TODO polylineDash
	// TODO polylineGlow
	// TODO image
	// TODO grid
	// TODO stripe
	// TODO Checkerboard
}
