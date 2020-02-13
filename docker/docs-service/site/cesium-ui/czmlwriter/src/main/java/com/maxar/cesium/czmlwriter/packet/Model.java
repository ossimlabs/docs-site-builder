package com.maxar.cesium.czmlwriter.packet;

import static com.maxar.cesium.czmlwriter.property.Property.writeAndClose;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.DoubleCesiumWriter;
import cesiumlanguagewriter.ModelCesiumWriter;
import cesiumlanguagewriter.UriCesiumWriter;

public interface Model extends
		Property<ModelCesiumWriter>
{
	public static Model create() {
		return writer -> {};
	}

	default Model show(
			final Property<BooleanCesiumWriter> show ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openShowProperty(),
							show);
		};
	}

	default Model uri(
			final Property<UriCesiumWriter> uri ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openGltfProperty(),
							uri);
		};
	}

	default Model scale(
			final Property<DoubleCesiumWriter> scale ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openScaleProperty(),
							scale);
		};
	}

	// TODO minimumPixelSize
	// TODO maximumPixelSize
	// TODO maximumScale
	// TODO ....
}
