package com.maxar.cesium.czmlwriter.packet;

import static com.maxar.cesium.czmlwriter.property.Property.writeAndClose;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.ColorCesiumWriter;
import cesiumlanguagewriter.DistanceDisplayConditionCesiumWriter;
import cesiumlanguagewriter.DoubleCesiumWriter;
import cesiumlanguagewriter.IntegerCesiumWriter;
import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.PolygonCesiumWriter;
import cesiumlanguagewriter.PositionListCesiumWriter;
import cesiumlanguagewriter.ShadowModeCesiumWriter;

public interface Polygon extends
		Property<PolygonCesiumWriter>
{
	public static Polygon create() {
		return writer -> {};
	}

	// TODO holes
	// TODO arcType - Default GEODESIC
	// TODO heightReference - Default NONE
	// TODO extrudedHeightReference - Default NONE
	// TODO classificationType

	// Default true
	default Polygon show(
			final Property<BooleanCesiumWriter> show ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openShowProperty(),
							show);
		};
	}

	default Polygon positions(
			final Property<PositionListCesiumWriter> positions ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPositionsProperty(),
							positions);
		};
	}

	// Default 0.0
	default Polygon height(
			final Property<DoubleCesiumWriter> height ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openHeightProperty(),
							height);
		};
	}

	default Polygon extrudedHeight(
			final Property<DoubleCesiumWriter> extrudedHeight ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openExtrudedHeightProperty(),
							extrudedHeight);
		};
	}

	// Default 0.0
	default Polygon stRotation(
			final Property<DoubleCesiumWriter> stRotation ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openStRotationProperty(),
							stRotation);
		};
	}

	// Default PI / 180.0
	default Polygon granularity(
			final Property<DoubleCesiumWriter> granularity ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openGranularityProperty(),
							granularity);
		};
	}

	// Default true
	default Polygon fill(
			final Property<BooleanCesiumWriter> fill ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openFillProperty(),
							fill);
		};
	}

	// Default solid white
	default Polygon material(
			final Property<MaterialCesiumWriter> material ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openMaterialProperty(),
							material);
		};
	}

	// Default false
	default Polygon outline(
			final Property<BooleanCesiumWriter> outline ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openOutlineProperty(),
							outline);
		};
	}

	// Default black
	default Polygon outlineColor(
			final Property<ColorCesiumWriter> outlineColor ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openOutlineColorProperty(),
							outlineColor);
		};
	}

	// Default 1.0
	default Polygon outlineWidth(
			final Property<DoubleCesiumWriter> outlineWidth ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openOutlineWidthProperty(),
							outlineWidth);
		};
	}

	// Default false
	default Polygon perPositionHeight(
			final Property<BooleanCesiumWriter> perPositionHeight ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPerPositionHeightProperty(),
							perPositionHeight);
		};
	}

	// Default true
	default Polygon closeTop(
			final Property<BooleanCesiumWriter> closeTop ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openCloseTopProperty(),
							closeTop);
		};
	}

	// Default true
	default Polygon closeBottom(
			final Property<BooleanCesiumWriter> closeBottom ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openCloseBottomProperty(),
							closeBottom);
		};
	}

	// Default DISABLED
	default Polygon shadows(
			final Property<ShadowModeCesiumWriter> shadows ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openShadowsProperty(),
							shadows);
		};
	}

	default Polygon distanceDisplayCondition(
			final Property<DistanceDisplayConditionCesiumWriter> distanceDisplayCondition ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openDistanceDisplayConditionProperty(),
							distanceDisplayCondition);
		};
	}

	// Default 0
	default Polygon zIndex(
			final Property<IntegerCesiumWriter> zIndex ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openZIndexProperty(),
							zIndex);
		};
	}
}
