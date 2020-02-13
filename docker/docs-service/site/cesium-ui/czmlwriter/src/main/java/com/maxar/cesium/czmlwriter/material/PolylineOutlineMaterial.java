package com.maxar.cesium.czmlwriter.material;

import static com.maxar.cesium.czmlwriter.property.Property.writeAndClose;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.ColorCesiumWriter;
import cesiumlanguagewriter.DoubleCesiumWriter;
import cesiumlanguagewriter.PolylineOutlineMaterialCesiumWriter;

public interface PolylineOutlineMaterial extends
		Property<PolylineOutlineMaterialCesiumWriter>
{
	public static PolylineOutlineMaterial create() {
		return writer -> {};
	}

	default PolylineOutlineMaterial color(
			final Property<ColorCesiumWriter> color ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openColorProperty(),
							color);
		};
	}

	default PolylineOutlineMaterial outlineColor(
			final Property<ColorCesiumWriter> outlineColor ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openOutlineColorProperty(),
							outlineColor);
		};
	}

	default PolylineOutlineMaterial outlineWidth(
			final Property<DoubleCesiumWriter> width ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openOutlineWidthProperty(),
							width);
		};
	}
}
