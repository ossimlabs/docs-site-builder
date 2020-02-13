package com.maxar.planning.model.image;

import java.awt.Color;
import java.math.BigInteger;
import java.util.Arrays;

import org.joda.time.Interval;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.packet.LayerControl;
import com.maxar.cesium.czmlwriter.packet.Polygon;
import com.maxar.cesium.czmlwriter.packet.TimelineControl;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.refvalue.BooleanRefValue;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.cesium.czmlwriter.types.PropertyInterval;
import com.maxar.common.utils.GeometryDeserializer;
import com.maxar.common.utils.GeometrySerializer;
import com.maxar.planning.model.czml.ImageFrameCzmlProperties;
import com.radiantblue.analytics.core.DateTimeFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageFrameModel
{
	private String imageId;
	private double niirs;
	private double gsdMeters;
	private long startTimeMillis;
	private long stopTimeMillis;

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	private Geometry polygon;

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	private Geometry centerCoord;

	public String produceCzml(
			final String parentPacketId,
			final String parentAssetName,
			final ImageFrameCzmlProperties properties ) {

		final Interval packetInterval = new Interval(
				DateTimeFactory.fromMillis(startTimeMillis),
				DateTimeFactory.fromMillis(stopTimeMillis));

		Packet packet = Packet.create()
				.id(imageId)
				.name("Image Frame " + imageId)
				.timelineControl(TimelineControl.create()
						.start(packetInterval.getStart())
						.end(packetInterval.getEnd())
						.group(parentAssetName + " Ops"))
				.layerControl(LayerControl.create()
						.showAsLayer(properties.isDisplayFrameInTree()))
				.availability(	packetInterval.getStart(),
								packetInterval.getEnd());

		if (parentPacketId != null) {
			packet = packet.parent(parentPacketId);
		}

		final Polygon imageFramePolygon = Polygon.create()
				.outline(BooleanRefValue.booleanValue(true))
				.outlineColor(ColorRefValue.color(new Color(
						new BigInteger(
								properties.getOutlineColor(),
								16).intValue(),
						true)))
				.outlineWidth(DoubleRefValue.number(properties.getOutlineWidth()))
				.fill(BooleanRefValue.booleanValue(properties.isFill()))
				.positions(Property.interval(Arrays.asList(new PropertyInterval<PositionList>(
						new Interval(
								startTimeMillis,
								stopTimeMillis),
						PositionList.geometry(polygon)))));

		return packet.polygon(imageFramePolygon)
				.writeString();
	}
}
