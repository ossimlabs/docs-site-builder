package com.maxar.cesium.czmlwriter.packet;

import java.util.List;

import com.maxar.cesium.czmlwriter.CesiumLanguageWriterUtils;
import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.property.deletable.DeletableProperty;
import com.maxar.cesium.czmlwriter.property.interpolation.InterpolatableProperty;
import com.maxar.cesium.czmlwriter.types.PositionVelocity;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.radiantblue.analytics.core.Vector3D;
import com.radiantblue.analytics.geodesy.EarthCenteredFrame;
import com.radiantblue.analytics.geodesy.LatLonAlt;

import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.Reference;

public interface Position extends
		InterpolatableProperty<PositionCesiumWriter, Position>,
		DeletableProperty<PositionCesiumWriter, Position>
{
	public static Position cartersian(
			final EarthCenteredFrame frame,
			final Vector3D position ) {
		return writer -> {
			writer.writeReferenceFrame(CesiumLanguageWriterUtils.referenceFrame(frame));
			writer.writeCartesian(CesiumLanguageWriterUtils.cartesian(position));
		};
	}

	public static Position cartesian(
			final EarthCenteredFrame frame,
			final List<TimeTaggedValue<Vector3D>> position ) {
		return writer -> {
			writer.writeReferenceFrame(CesiumLanguageWriterUtils.referenceFrame(frame));
			writer.writeCartesian(	TimeTaggedValue.listAsJulianDates(position),
									TimeTaggedValue.listAsValues(	position,
																	CesiumLanguageWriterUtils::cartesian));
		};
	}

	public static Position cartesianVelocity(
			final EarthCenteredFrame frame,
			final PositionVelocity positionVelocity ) {
		return writer -> {
			writer.writeReferenceFrame(CesiumLanguageWriterUtils.referenceFrame(frame));
			writer.writeCartesianVelocity(positionVelocity.asMotion1());
		};
	}

	public static Position cartesianVelocity(
			final EarthCenteredFrame frame,
			final List<TimeTaggedValue<PositionVelocity>> positionVelocity ) {
		return writer -> {
			writer.writeReferenceFrame(CesiumLanguageWriterUtils.referenceFrame(frame));
			writer.writeCartesianVelocity(	TimeTaggedValue.listAsJulianDates(positionVelocity),
											TimeTaggedValue.listAsValues(	positionVelocity,
																			PositionVelocity::asMotion1));
		};
	}

	public static Position cartographicDegrees(
			final LatLonAlt position ) {
		return writer -> writer.writeCartographicDegrees(CesiumLanguageWriterUtils.cartographicDegrees(position));
	}

	public static Position cartographicDegrees(
			final List<TimeTaggedValue<LatLonAlt>> position ) {
		return writer -> writer.writeCartographicDegrees(	TimeTaggedValue.listAsJulianDates(position),
															TimeTaggedValue.listAsValues(	position,
																							CesiumLanguageWriterUtils::cartographicDegrees));
	}

	public static Position cartographicRadians(
			final LatLonAlt position ) {
		return writer -> writer.writeCartographicRadians(CesiumLanguageWriterUtils.cartographicRadians(position));
	}

	public static Position cartographicRadians(
			final List<TimeTaggedValue<LatLonAlt>> position ) {
		return writer -> writer.writeCartographicRadians(	TimeTaggedValue.listAsJulianDates(position),
															TimeTaggedValue.listAsValues(	position,
																							CesiumLanguageWriterUtils::cartographicRadians));
	}

	public static Position reference(
			final Reference reference ) {
		return writer -> writer.writeReference(reference);
	}

	@Override
	default public Position wrap(
			final Property<PositionCesiumWriter> property ) {
		return writer -> property.write(writer);
	}
}
