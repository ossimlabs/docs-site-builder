package com.maxar.cesium.czmlwriter.packet;

import static com.maxar.cesium.czmlwriter.property.Property.writeAndClose;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.ColorCesiumWriter;
import cesiumlanguagewriter.DistanceDisplayConditionCesiumWriter;
import cesiumlanguagewriter.DoubleCesiumWriter;
import cesiumlanguagewriter.HeightReferenceCesiumWriter;
import cesiumlanguagewriter.NearFarScalarCesiumWriter;
import cesiumlanguagewriter.PointCesiumWriter;

public interface Point extends
		Property<PointCesiumWriter>
{
	public static Point create() {
		return writer -> {};
	}

	// Default true
	default Point show(
			final Property<BooleanCesiumWriter> show ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openShowProperty(),
							show);
		};
	}

	// Default 1.0
	default Point pixelSize(
			final Property<DoubleCesiumWriter> pixelSize ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPixelSizeProperty(),
							pixelSize);
		};
	}

	// Default NONE
	default Point heightReference(
			final Property<HeightReferenceCesiumWriter> heightReference ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openHeightReferenceProperty(),
							heightReference);
		};
	}

	// Default white
	default Point color(
			final Property<ColorCesiumWriter> color ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openColorProperty(),
							color);
		};
	}

	// Default black
	default Point outlineColor(
			final Property<ColorCesiumWriter> outlineColor ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openOutlineColorProperty(),
							outlineColor);
		};
	}

	// Default 0.0
	default Point outlineWidth(
			final Property<DoubleCesiumWriter> outlineWidth ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openOutlineWidthProperty(),
							outlineWidth);
		};
	}

	default Point scaleByDistance(
			final Property<NearFarScalarCesiumWriter> scaleByDistance ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openScaleByDistanceProperty(),
							scaleByDistance);
		};
	}

	default Point translucencyByDistance(
			final Property<NearFarScalarCesiumWriter> translucencyByDistance ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openTranslucencyByDistanceProperty(),
							translucencyByDistance);
		};
	}

	default Point distanceDisplayCondition(
			final Property<DistanceDisplayConditionCesiumWriter> distanceDisplayCondition ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openDistanceDisplayConditionProperty(),
							distanceDisplayCondition);
		};
	}

	// Default 0.0
	default Point disableDepthTestDistance(
			final Property<DoubleCesiumWriter> disableDepthTestDistance ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openDisableDepthTestDistanceProperty(),
							disableDepthTestDistance);
		};
	}
}
