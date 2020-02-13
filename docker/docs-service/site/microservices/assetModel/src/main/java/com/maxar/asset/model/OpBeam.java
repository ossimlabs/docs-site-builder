package com.maxar.asset.model;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxar.asset.model.czml.AssetSmearCzmlProperties;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.packet.Polyline;
import com.maxar.cesium.czmlwriter.packet.Position;
import com.maxar.cesium.czmlwriter.packet.TimelineControl;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.common.utils.GeometryDeserializer;
import com.maxar.common.utils.GeometrySerializer;
import com.radiantblue.analytics.core.DateTimeFactory;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.LatLonAlt;

import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpBeam
{
	private String startTimeISO8601;
	private String stopTimeISO8601;

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	private Geometry beamCentroidGeo;

	public String produceCzml(
			final String parentId,
			final String assetName,
			final String assetId,
			final int beamCount,
			final AssetSmearCzmlProperties properties,
			final Color opColor ) {

		final DateTime startTime = new DateTime(
				startTimeISO8601);
		final DateTime stopTime = new DateTime(
				stopTimeISO8601);

		final Interval beamInterval = new Interval(
				startTime,
				stopTime.plusSeconds(properties.getBeamDurationSec()));

		final List<String> czmlStringList = new ArrayList<>();

		final String beamPacketContainerId = UUID.randomUUID()
				.toString();

		final Packet beamPacketContainer = Packet.create()
				.id(beamPacketContainerId)
				.name(assetName + "-Beam " + beamCount)
				.parent(assetId)
				.availability(	beamInterval.getStart(),
								beamInterval.getEnd());

		czmlStringList.add(beamPacketContainer.writeString());

		final com.maxar.cesium.czmlwriter.packet.Point centroidPoint = com.maxar.cesium.czmlwriter.packet.Point.create()
				.color(ColorRefValue.color(opColor))
				.pixelSize(DoubleRefValue.number(properties.getCentroidPixelSize()));

		Packet centroidPacket = Packet.create()
				.id(assetName + "-Centroid " + beamCount)
				.name(assetName + "-Centroid " + beamCount)
				.parent(beamPacketContainerId)
				.availability(	beamInterval.getStart(),
								beamInterval.getEnd())
				.position(Position.cartographicDegrees(new LatLonAlt(
						Angle.fromDegrees(beamCentroidGeo.getCentroid()
								.getY()),
						Angle.fromDegrees(beamCentroidGeo.getCentroid()
								.getX()),
						Length.Zero())))
				.point(centroidPoint);

		if (properties.isDisplayCentroidOnTimeline()) {
			centroidPacket = centroidPacket.timelineControl(TimelineControl.create()
					.start(DateTimeFactory.fromMillis(startTime.getMillis()))
					.end(DateTimeFactory.fromMillis(stopTime.getMillis()))
					.group(assetName + " Beams"));
		}

		czmlStringList.add(centroidPacket.writeString());

		// create packet for line between asset positions and centroid
		final Packet laserPacket = Packet.create()
				.id(assetName + "-Laser " + beamCount)
				.name(assetName + "-Laser " + beamCount)
				.parent(beamPacketContainerId)
				.availability(	beamInterval.getStart(),
								beamInterval.getEnd())
				.polyline(Polyline.create()
						.material(PolylineMaterial.create()
								.polylineOutline(PolylineOutlineMaterial.create()
										.color(ColorRefValue.color(new Color(
												new BigInteger(
														properties.getBeamColor(),
														16).intValue(),
												true)))
										.outlineColor(ColorRefValue.color(new Color(
												new BigInteger(
														properties.getBeamColor(),
														16).intValue(),
												true)))
										.outlineWidth(DoubleRefValue.number(properties.getBeamOutlineWidth()))))
						.positions(PositionList.references(Arrays.asList(	new Reference(
								assetId,
								PacketCesiumWriter.PositionPropertyName),
																			new Reference(
																					assetName + "-Centroid "
																							+ beamCount,
																					PacketCesiumWriter.PositionPropertyName)))));

		czmlStringList.add(laserPacket.writeString());

		return czmlStringList.stream()
				.collect(Collectors.joining(", "));
	}
}
