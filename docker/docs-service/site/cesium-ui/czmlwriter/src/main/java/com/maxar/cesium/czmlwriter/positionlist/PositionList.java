package com.maxar.cesium.czmlwriter.positionlist;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import com.maxar.cesium.czmlwriter.CesiumLanguageWriterUtils;
import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.property.deletable.DeletableProperty;
import com.radiantblue.analytics.core.Vector3D;
import com.radiantblue.analytics.geodesy.EarthCenteredFrame;
import com.radiantblue.analytics.geodesy.LatLonAlt;

import cesiumlanguagewriter.PositionListCesiumWriter;
import cesiumlanguagewriter.Reference;

public interface PositionList extends
		Property<PositionListCesiumWriter>,
		DeletableProperty<PositionListCesiumWriter, PositionList>
{
	public static PositionList create() {
		return writer -> {};
	}

	public static PositionList cartersian(
			final EarthCenteredFrame frame,
			final List<Vector3D> positions ) {
		return writer -> {
			writer.writeReferenceFrame(CesiumLanguageWriterUtils.referenceFrame(frame));
			writer.writeCartesian(positions.stream()
					.map(vector -> CesiumLanguageWriterUtils.cartesian(vector))
					.collect(Collectors.toList()));
		};
	}

	public static PositionList cartographicDegrees(
			final List<LatLonAlt> positions ) {
		return writer -> writer.writeCartographicDegrees(positions.stream()
				.map(latLonAlt -> CesiumLanguageWriterUtils.cartographicDegrees(latLonAlt))
				.collect(Collectors.toList()));
	}

	public static PositionList cartographicRadians(
			final List<LatLonAlt> positions ) {
		return writer -> writer.writeCartographicRadians(positions.stream()
				.map(latLonAlt -> CesiumLanguageWriterUtils.cartographicRadians(latLonAlt))
				.collect(Collectors.toList()));
	}

	public static PositionList geometry(
			final Geometry geometry ) {
		return writer -> coordinates(geometry.getCoordinates()).write(writer);
	}

	public static PositionList coordinates(
			final Coordinate[] positions ) {
		return writer -> writer.writeCartographicDegrees(Arrays.stream(positions)
				.map(coordinate -> CesiumLanguageWriterUtils.coordinate(coordinate))
				.collect(Collectors.toList()));
	}

	public static PositionList references(
			final List<Reference> references ) {
		return writer -> writer.writeReferences(references);
	}

	@Override
	default public PositionList wrap(
			final Property<PositionListCesiumWriter> property ) {
		return writer -> property.write(writer);
	}
}
