package com.maxar.common.czml;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.offset.EyeOffset;
import com.maxar.cesium.czmlwriter.packet.Billboard;
import com.maxar.cesium.czmlwriter.packet.Path;
import com.maxar.cesium.czmlwriter.packet.Position;
import com.maxar.cesium.czmlwriter.packet.TimelineControl;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.radiantblue.analytics.core.Vector3D;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.EarthCenteredFrame;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.LatLonAlt;
import com.radiantblue.analytics.mechanics.IStateVectorProvider;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

public class VehiclePositionCZMLUtils
{

	public static String writeStateVectorsToCZML(
			final List<StateVectorsInFrame> svif,
			final String id,
			final VehiclePositionCzmlProperties properties ) {

		final List<TimeTaggedValue<Vector3D>> svs = new ArrayList<>();
		final List<TimeTaggedValue<LatLonAlt>> groundPositions = new ArrayList<>();
		DateTime start = null;
		DateTime end = null;
		for (final StateVectorsInFrame sv : svif) {
			svs.add(new TimeTaggedValue<>(
					sv.atTime(),
					sv.getPosition()));
			final GeodeticPoint point = sv.geodeticPosition();
			groundPositions.add(new TimeTaggedValue<>(
					sv.atTime(),
					new LatLonAlt(
							point.latitude(),
							point.longitude(),
							Length.Zero())));

			if ((start == null) || sv.atTime()
					.isBefore(start)) {
				start = sv.atTime();
			}
			if ((end == null) || sv.atTime()
					.isAfter(end)) {
				end = sv.atTime();
			}
		}

		final TimelineControl timelineControl = TimelineControl.create()
				.start(start)
				.end(end)
				.group("Vehicle Data");

		final StringBuilder czmlString = new StringBuilder();

		final Packet assetPacket = Packet.create()
				.id(id)
				.name(id)
				.timelineControl(timelineControl);

		czmlString.append(assetPacket.writeString() + ",");

		final Packet vehiclePacket = Packet.create()
				.id(id + " - position")
				.name(id + " - position")
				.parent(id)
				.billboard(Billboard.create()
						.eyeOffset(EyeOffset.cartesian(Vector3D.zero())))
				.path(Path.create()
						.width(DoubleRefValue.number(properties.getPositionLinewidth()))
						.leadTime(DoubleRefValue.number(0.0))
						.trailTime(DoubleRefValue.number(properties.getTrailTime()))
						.material(PolylineMaterial.create()
								.polylineOutline(PolylineOutlineMaterial.create()
										.color(ColorRefValue.color(new Color(
												new BigInteger(
														properties.getPositionColor(),
														16).intValue(),
												true)))
										.outlineColor(ColorRefValue.color(new Color(
												new BigInteger(
														properties.getPositionOutlineColor(),
														16).intValue(),
												true)))
										.outlineWidth(DoubleRefValue.number(properties.getPositionOutlineWidth())))))
				.position(Position.cartesian(	EarthCenteredFrame.ECEF,
												svs));

		czmlString.append(vehiclePacket.writeString() + ",");

		final Packet groundTracePacket = Packet.create()
				.id(id + " - ground trace")
				.name(id + " - ground trace")
				.parent(id)
				.billboard(Billboard.create()
						.eyeOffset(EyeOffset.cartesian(Vector3D.zero())))
				.path(Path.create()
						.width(DoubleRefValue.number(properties.getGroundTraceLinewidth()))
						.leadTime(DoubleRefValue.number(0.0))
						.trailTime(DoubleRefValue.number(properties.getTrailTime()))
						.material(PolylineMaterial.create()
								.polylineOutline(PolylineOutlineMaterial.create()
										.color(ColorRefValue.color(new Color(
												new BigInteger(
														properties.getGroundTraceColor(),
														16).intValue(),
												true)))
										.outlineColor(ColorRefValue.color(new Color(
												new BigInteger(
														properties.getGroundTraceOutlineColor(),
														16).intValue(),
												true)))
										.outlineWidth(DoubleRefValue.number(properties.getGroundTraceOutlineWidth())))))
				.position(Position.cartographicRadians(groundPositions));

		czmlString.append(groundTracePacket.writeString());

		return czmlString.toString();
	}

	public static List<StateVectorsInFrame> generatePositionVectors(
			final DateTime start,
			final DateTime end,
			final long samplingInterval,
			final IStateVectorProvider svp ) {

		final List<StateVectorsInFrame> svif = new ArrayList<>();

		DateTime atTime = new DateTime(
				start);

		while (atTime.isBefore(end)) {
			svif.add(svp.getStateVectors(	atTime,
											EarthCenteredFrame.ECEF));

			atTime = atTime.plus(samplingInterval);

			// Add the last moment of the timeframe as the final sample
			if (!atTime.isBefore(end)) {
				atTime = end;
				svif.add(svp.getStateVectors(	atTime,
												EarthCenteredFrame.ECEF));
			}
		}

		return svif;
	}

}
