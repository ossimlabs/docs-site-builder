package com.maxar.cesium.czmlwriter.packet;

import static com.maxar.cesium.czmlwriter.property.Property.writeAndClose;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.BackgroundPaddingCesiumWriter;
import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.ColorCesiumWriter;
import cesiumlanguagewriter.DoubleCesiumWriter;
import cesiumlanguagewriter.EyeOffsetCesiumWriter;
import cesiumlanguagewriter.FontCesiumWriter;
import cesiumlanguagewriter.HorizontalOriginCesiumWriter;
import cesiumlanguagewriter.LabelCesiumWriter;
import cesiumlanguagewriter.LabelStyleCesiumWriter;
import cesiumlanguagewriter.PixelOffsetCesiumWriter;
import cesiumlanguagewriter.StringCesiumWriter;
import cesiumlanguagewriter.VerticalOriginCesiumWriter;

public interface Label extends
		Property<LabelCesiumWriter>
{
	public static Label create() {
		return writer -> {};
	}

	default Label show(
			final Property<BooleanCesiumWriter> show ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openShowProperty(),
							show);
		};
	}

	default Label text(
			final Property<StringCesiumWriter> text ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openTextProperty(),
							text);
		};
	}

	default Label font(
			final Property<FontCesiumWriter> font ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openFontProperty(),
							font);
		};
	}

	default Label style(
			final Property<LabelStyleCesiumWriter> style ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openStyleProperty(),
							style);
		};
	}

	default Label scale(
			final Property<DoubleCesiumWriter> scale ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openScaleProperty(),
							scale);
		};
	}

	default Label showBackground(
			final Property<BooleanCesiumWriter> showBackground ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openShowBackgroundProperty(),
							showBackground);
		};
	}

	default Label backgroundColor(
			final Property<ColorCesiumWriter> backgroundColor ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openBackgroundColorProperty(),
							backgroundColor);
		};
	}

	default Label backgroundPadding(
			final Property<BackgroundPaddingCesiumWriter> backgroundPadding ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openBackgroundPaddingProperty(),
							backgroundPadding);
		};
	}

	default Label pixelOffset(
			final Property<PixelOffsetCesiumWriter> pixelOffset ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPixelOffsetProperty(),
							pixelOffset);
		};
	}

	default Label eyeOffset(
			final Property<EyeOffsetCesiumWriter> eyeOffset ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openEyeOffsetProperty(),
							eyeOffset);
		};
	}

	default Label horizontalOrigin(
			final Property<HorizontalOriginCesiumWriter> horizontalOrigin ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openHorizontalOriginProperty(),
							horizontalOrigin);
		};
	}

	default Label verticalOrigin(
			final Property<VerticalOriginCesiumWriter> verticalOrigin ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openVerticalOriginProperty(),
							verticalOrigin);
		};
	}

	// TODO HeightReference

	default Label fillColor(
			final Property<ColorCesiumWriter> fillColor ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openFillColorProperty(),
							fillColor);
		};
	}

	default Label outlineColor(
			final Property<ColorCesiumWriter> outlineColor ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openOutlineColorProperty(),
							outlineColor);
		};
	}

	default Label outlineWidth(
			final Property<DoubleCesiumWriter> outlineWidth ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openOutlineWidthProperty(),
							outlineWidth);
		};
	}

	// TODO translucencyByDistance
	// TODO pixelOffsetScaleByDistance
	// TODO scalByDistance
	// TODO distanceDisplayCondition

	default Label disableDepthTestDistance(
			final Property<DoubleCesiumWriter> disableDepthTestDistance ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openDisableDepthTestDistanceProperty(),
							disableDepthTestDistance);
		};
	}
}
