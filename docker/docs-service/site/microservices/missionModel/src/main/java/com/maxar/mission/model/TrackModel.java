package com.maxar.mission.model;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.Interval;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.packet.Path;
import com.maxar.cesium.czmlwriter.packet.Polyline;
import com.maxar.cesium.czmlwriter.packet.Position;
import com.maxar.cesium.czmlwriter.packet.TimelineControl;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.refvalue.BooleanRefValue;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.maxar.common.utils.GeometryDeserializer;
import com.maxar.common.utils.GeometrySerializer;
import com.maxar.mission.model.czml.TrackCzmlProperties;
import com.radiantblue.analytics.core.DateTimeFactory;
import com.radiantblue.analytics.geodesy.LatLonAlt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackModel
{
	private String id;
	private String name;

	@Builder.Default
	private List<TrackNodeModel> trackNodes = new ArrayList<>();

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	private Geometry trackGeo;

	public String produceCzml(
			final TrackCzmlProperties properties ) {
		return produceCzml(	null, // parentId
							properties,
							null, // onStationMillis
							null); // offStationMillis
	}

	public String produceCzml(
			final String parentId,
			final TrackCzmlProperties properties,
			final Long onStationMillis,
			final Long offStationMillis ) {
		final String packetId = UUID.randomUUID()
				.toString();

		Packet packet = Packet.create()
				.id("Track Geo " + name)
				.name("Track Geo " + name);

		if ((onStationMillis != null) && (offStationMillis != null)) {
			final Interval packetInterval = new Interval(
					DateTimeFactory.fromMillis(onStationMillis),
					DateTimeFactory.fromMillis(offStationMillis));

			packet = packet.timelineControl(TimelineControl.create()
					.start(packetInterval.getStart())
					.end(packetInterval.getEnd())
					.group(id))
					.availability(	packetInterval.getStart(),
									packetInterval.getEnd());
		}

		if (parentId != null) {
			packet = packet.parent(parentId);
		}

		final List<String> trackDataCzmlList = new ArrayList<>();

		final Polyline polyline = Polyline.create()
				.width(DoubleRefValue.number(properties.getTrackWidth()))
				.clampToGround(BooleanRefValue.booleanValue(true))
				.material(PolylineMaterial.create()
						.polylineOutline(PolylineOutlineMaterial.create()
								.color(ColorRefValue.color(new Color(
										new BigInteger(
												properties.getTrackColor(),
												16).intValue(),
										true)))
								.outlineColor(ColorRefValue.color(new Color(
										new BigInteger(
												properties.getTrackOutlineColor(),
												16).intValue(),
										true)))
								.outlineWidth(DoubleRefValue.number(properties.getTrackOutlineWidth()))))
				.positions(PositionList.geometry(trackGeo));

		trackDataCzmlList.add(packet.polyline(polyline)
				.writeString());

		final boolean isTrackGeoBuilt = !trackNodes.isEmpty() && (onStationMillis != null);

		if (isTrackGeoBuilt) {
			final List<TimeTaggedValue<LatLonAlt>> positions = new ArrayList<>();

			positions.addAll(trackNodes.stream()
					.map(trackNodeModel -> trackNodeModel.produceTimeTaggedValue(onStationMillis))
					.collect(Collectors.toList()));

			final String trackPacketId = UUID.randomUUID()
					.toString();

			Packet trackTrace = Packet.create()
					.id(trackPacketId)
					.name("Waypoints " + name);

			if (parentId != null) {
				trackTrace = trackTrace.parent(parentId);
			}
			else {
				trackTrace = trackTrace.parent(packetId);
			}

			final Path path = Path.create()
					.width(DoubleRefValue.number(properties.getTrackTraceWidth()))
					.leadTime(DoubleRefValue.number(0.0))
					.trailTime(DoubleRefValue.number(properties.getTrackNodeTrailTimeSec()))
					.material(PolylineMaterial.create()
							.polylineOutline(PolylineOutlineMaterial.create()
									.color(ColorRefValue.color(new Color(
											new BigInteger(
													properties.getTrackTraceColor(),
													16).intValue(),
											true)))
									.outlineColor(ColorRefValue.color(new Color(
											new BigInteger(
													properties.getTrackTraceOutlineColor(),
													16).intValue(),
											true)))
									.outlineWidth(DoubleRefValue.number(properties.getTrackTraceOutlineWidth()))));

			trackDataCzmlList.add(trackTrace.path(path)
					.position(Position.cartographicDegrees(positions))
					.writeString());
		}

		return trackDataCzmlList.stream()
				.collect(Collectors.joining(","));
	}
}
