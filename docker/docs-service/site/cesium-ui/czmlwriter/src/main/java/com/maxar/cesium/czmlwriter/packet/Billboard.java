package com.maxar.cesium.czmlwriter.packet;

import static com.maxar.cesium.czmlwriter.property.Property.writeAndClose;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.BillboardCesiumWriter;
import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.ColorCesiumWriter;
import cesiumlanguagewriter.DoubleCesiumWriter;
import cesiumlanguagewriter.EyeOffsetCesiumWriter;
import cesiumlanguagewriter.HorizontalOriginCesiumWriter;
import cesiumlanguagewriter.PixelOffsetCesiumWriter;
import cesiumlanguagewriter.UriCesiumWriter;
import cesiumlanguagewriter.VerticalOriginCesiumWriter;

public interface Billboard extends
		Property<BillboardCesiumWriter>
{

	public static Billboard create() {
		return writer -> {};
	}

	default Billboard show(
			final Property<BooleanCesiumWriter> show ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openShowProperty(),
							show);
		};
	}

	default Billboard image(
			final Property<UriCesiumWriter> image ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openImageProperty(),
							image);
		};
	}

	default Billboard scale(
			final Property<DoubleCesiumWriter> scale ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openScaleProperty(),
							scale);
		};
	}

	default Billboard pixelOffset(
			final Property<PixelOffsetCesiumWriter> pixelOffset ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPixelOffsetProperty(),
							pixelOffset);
		};
	}

	default Billboard eyeOffset(
			final Property<EyeOffsetCesiumWriter> eyeOffset ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openEyeOffsetProperty(),
							eyeOffset);
		};
	}

	default Billboard horizontalOrigin(
			final Property<HorizontalOriginCesiumWriter> horizontalOrigin ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openHorizontalOriginProperty(),
							horizontalOrigin);
		};
	}

	default Billboard verticalOrigin(
			final Property<VerticalOriginCesiumWriter> verticalOrigin ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openVerticalOriginProperty(),
							verticalOrigin);
		};
	}

	// TODO heightReference

	default Billboard color(
			final Property<ColorCesiumWriter> color ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openColorProperty(),
							color);
		};
	}

	// TODO rotation
	// TODO alignedAxis
	// TODO sizeInMeters

	default Billboard width(
			final Property<DoubleCesiumWriter> width ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openWidthProperty(),
							width);
		};
	}

	default Billboard height(
			final Property<DoubleCesiumWriter> height ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openHeightProperty(),
							height);
		};
	}

	// TODO scaleByDistance
	// TODO translucencyByDistance
	// TODO pixelOffsetScaleByDistance
	// TODO imageSubRegion
	// TODO distanceDisplayCondition
	// TODO disableDepthTestDistance
}
