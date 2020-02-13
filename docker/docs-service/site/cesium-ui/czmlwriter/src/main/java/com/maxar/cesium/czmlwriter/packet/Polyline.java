package com.maxar.cesium.czmlwriter.packet;

import static com.maxar.cesium.czmlwriter.property.Property.writeAndClose;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.DoubleCesiumWriter;
import cesiumlanguagewriter.IntegerCesiumWriter;
import cesiumlanguagewriter.PolylineCesiumWriter;
import cesiumlanguagewriter.PolylineMaterialCesiumWriter;
import cesiumlanguagewriter.PositionListCesiumWriter;;

public interface Polyline extends
		Property<PolylineCesiumWriter>
{
	public static Polyline create() {
		return writer -> {};
	}

	default Polyline show(
			final Property<BooleanCesiumWriter> show ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openShowProperty(),
							show);
		};
	}

	default Polyline positions(
			final Property<PositionListCesiumWriter> positions ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPositionsProperty(),
							positions);
		};
	}

	// TODO ArcType

	default Polyline width(
			final Property<DoubleCesiumWriter> width ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openWidthProperty(),
							width);
		};
	}

	default Polyline granularity(
			final Property<DoubleCesiumWriter> granularity ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openGranularityProperty(),
							granularity);
		};
	}

	default Polyline material(
			final Property<PolylineMaterialCesiumWriter> material ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openMaterialProperty(),
							material);
		};
	}

	default Polyline followSurface(
			final Property<BooleanCesiumWriter> followSurface ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openFollowSurfaceProperty(),
							followSurface);
		};
	}

	// TODO ShadowMode

	default Polyline depthFailMaterial(
			final Property<PolylineMaterialCesiumWriter> depthFailMaterial ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openDepthFailMaterialProperty(),
							depthFailMaterial);
		};
	}

	// TODO distanceDisplayCondition

	default Polyline clampToGround(
			final Property<BooleanCesiumWriter> clampToGround ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openClampToGroundProperty(),
							clampToGround);
		};
	}

	// TODO classificationType

	default Polyline zIndex(
			final Property<IntegerCesiumWriter> zIndex ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openZIndexProperty(),
							zIndex);
		};
	}
}
