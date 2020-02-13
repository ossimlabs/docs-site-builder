package com.maxar.planning.model.tasking;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.Interval;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.packet.LayerControl;
import com.maxar.cesium.czmlwriter.packet.Point;
import com.maxar.cesium.czmlwriter.packet.Position;
import com.maxar.cesium.czmlwriter.packet.TimelineControl;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.common.utils.GeometryDeserializer;
import com.maxar.common.utils.GeometrySerializer;
import com.maxar.planning.model.link.LinkModel;
import com.maxar.planning.model.czml.TaskingCzmlProperties;
import com.radiantblue.analytics.core.DateTimeFactory;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.LatLonAlt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskingModel
{
	private String missionId;

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	private Geometry taskingCoord;

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	private Geometry lookAtCoord;

	private String sensorName;
	private String sensorType;
	private String sensorMode;
	private Integer sensorPed;
	private Integer sensorAdHoc;
	private Integer sensorSlots;
	private Long earliestImageTimeMillis;
	private Long latestImageTimeMillis;
	private Integer numImages;
	private Integer priority;
	private long totTimeMillis;
	private String taskingAuthority;
	private String taskStandingAdHocFlag;
	private String scene;
	private Boolean complex;
	private LinkModel link;

	public String produceCzml(
			final TaskingCzmlProperties properties ) {
		return produceCzml(	null,
							properties);
	}

	public String produceCzml(
			final String parentId,
			final TaskingCzmlProperties properties ) {
		final Interval packetInterval = new Interval(
				DateTimeFactory.fromMillis(totTimeMillis),
				DateTimeFactory.fromMillis(totTimeMillis + properties.getAirborneTaskingDurationMillis()));

		final List<String> czmlStringList = new ArrayList<>();

		Packet missionParentPacket = Packet.create()
				.id(missionId)
				.name(missionId);

		final Packet packet = Packet.create()
				.id(missionId + " " + link.getCrId() + " " + link.getTargetId())
				.name(link.getCrId() + " " + link.getTargetId())
				.parent(missionId)
				.timelineControl(TimelineControl.create()
						.start(packetInterval.getStart())
						.end(packetInterval.getEnd())
						.group(missionId + " Taskings"))
				.layerControl(LayerControl.create()
						.showAsLayer(properties.isDisplayTaskingInTree()))
				.availability(	packetInterval.getStart(),
								packetInterval.getEnd());

		if (parentId != null) {
			missionParentPacket = missionParentPacket.parent(parentId);
		}

		czmlStringList.add(missionParentPacket.writeString());

		if (taskingCoord != null) {
			final Position taskingPos = Position.cartographicDegrees(new LatLonAlt(
					Angle.fromDegrees(taskingCoord.getCoordinate().y),
					Angle.fromDegrees(taskingCoord.getCoordinate().x),
					Length.Zero()));

			final Point taskingCoordPoint = Point.create()
					.color(ColorRefValue.color(new Color(
							new BigInteger(
									properties.getColor(),
									16).intValue(),
							true)))
					.pixelSize(DoubleRefValue.number(properties.getPixelSize()))
					.outlineColor(ColorRefValue.color(new Color(
							new BigInteger(
									properties.getOutlineColor(),
									16).intValue(),
							true)))
					.outlineWidth(DoubleRefValue.number(properties.getOutlineWidth()));

			czmlStringList.add(packet.position(taskingPos)
					.point(taskingCoordPoint)
					.writeString());
		}

		return czmlStringList.stream()
				.collect(Collectors.joining(", "));
	}
}
