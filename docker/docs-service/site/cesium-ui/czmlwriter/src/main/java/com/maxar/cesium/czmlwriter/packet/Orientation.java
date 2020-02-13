package com.maxar.cesium.czmlwriter.packet;

import java.util.List;
import java.util.stream.Collectors;

import com.maxar.cesium.czmlwriter.CesiumLanguageWriterUtils;
import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.property.interpolation.InterpolatableProperty;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.radiantblue.analytics.core.Quaternion;

import cesiumlanguagewriter.OrientationCesiumWriter;

public interface Orientation extends
		InterpolatableProperty<OrientationCesiumWriter, Orientation>
{

	public static Orientation quaternion(
			final Quaternion quaternion ) {
		return writer -> writer.writeUnitQuaternion(CesiumLanguageWriterUtils.unitQuaternion(quaternion));
	}

	public static Orientation quaternion(
			final List<TimeTaggedValue<Quaternion>> quaternions ) {
		return writer -> writer.writeUnitQuaternion(TimeTaggedValue.listAsJulianDates(quaternions),
													TimeTaggedValue.listAsValues(quaternions)
															.stream()
															.map(quaternion -> CesiumLanguageWriterUtils
																	.unitQuaternion(quaternion))
															.collect(Collectors.toList()));
	}

	public static Orientation reference(
			final String reference ) {
		return writer -> writer.writeReference(reference);

	}

	public static Orientation reference(
			final String identifier,
			final String propertyName ) {
		return writer -> writer.writeReference(	identifier,
												propertyName);

	}

	public static Orientation reference(
			final String identifier,
			final List<String> propertyNames ) {
		return writer -> writer.writeReference(	identifier,
												propertyNames.toArray(new String[propertyNames.size()]));

	}

	public static Orientation velocityReference(
			final String reference ) {
		return writer -> writer.writeVelocityReference(reference);

	}

	public static Orientation velocityReference(
			final String identifier,
			final String propertyName ) {
		return writer -> writer.writeVelocityReference(	identifier,
														propertyName);

	}

	public static Orientation velocityReference(
			final String identifier,
			final List<String> propertyNames ) {
		return writer -> writer.writeVelocityReference(	identifier,
														propertyNames.toArray(new String[propertyNames.size()]));
	}

	@Override
	public void write(
			final OrientationCesiumWriter writer );

	@Override
	default public Orientation wrap(
			final Property<OrientationCesiumWriter> property ) {
		return writer -> {
			property.write(writer);
		};
	}
}
