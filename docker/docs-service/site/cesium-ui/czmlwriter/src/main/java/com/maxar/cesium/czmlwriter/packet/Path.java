package com.maxar.cesium.czmlwriter.packet;

import static com.maxar.cesium.czmlwriter.property.Property.writeAndClose;

import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.BooleanCesiumWriter;
import cesiumlanguagewriter.DoubleCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PolylineMaterialCesiumWriter;

public interface Path extends
		Property<PathCesiumWriter>
{
	public static Path create() {
		return writer -> {};
	}

	default Path show(
			final Property<BooleanCesiumWriter> show ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openShowProperty(),
							show);
		};
	}

	default Path leadTime(
			final Property<DoubleCesiumWriter> leadTime ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openLeadTimeProperty(),
							leadTime);
		};
	}

	default Path trailTime(
			final Property<DoubleCesiumWriter> trailTime ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openTrailTimeProperty(),
							trailTime);
		};
	}

	default Path width(
			final Property<DoubleCesiumWriter> width ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openWidthProperty(),
							width);
		};
	}

	default Path resolution(
			final Property<DoubleCesiumWriter> resolution ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openResolutionProperty(),
							resolution);
		};
	}

	default Path material(
			final Property<PolylineMaterialCesiumWriter> material ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openMaterialProperty(),
							material);
		};
	}
}
