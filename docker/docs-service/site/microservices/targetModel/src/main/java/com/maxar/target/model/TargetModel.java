package com.maxar.target.model;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.packet.Polyline;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.refvalue.BooleanRefValue;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.common.utils.GeometryDeserializer;
import com.maxar.common.utils.GeometrySerializer;
import com.maxar.target.model.czml.TargetCzmlProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetModel
{
	protected TargetType targetType;
	protected String targetId;
	protected String targetName;
	protected String description;
	protected String countryCode;
	protected String geoRegion;
	
	@Builder.Default
	protected boolean estimated = false;
	
	@Builder.Default
	protected DateTime czmlStartTime = null;
	
	@Builder.Default
	protected DateTime czmlStopTime = null;

	@Builder.Default
	protected OrderOfBattle orderOfBattle = OrderOfBattle.GROUND;

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	protected Geometry geometry;

	public String produceCzml() {
		return produceCzml(	null,
							new TargetCzmlProperties());
	}

	public String produceCzml(
			final TargetCzmlProperties properties ) {
		return produceCzml(	null,
							properties);
	}

	public String produceCzml(
			final String parentId,
			final TargetCzmlProperties properties ) {
		final String packetId = targetId + ":" + targetName;
		Packet packet = Packet.create()
				.id(packetId)
				.name(targetId + ":" + targetName);

		if (parentId != null) {
			packet = packet.parent(parentId);
		}

		if ((czmlStartTime != null) && (czmlStopTime != null)) {
			packet = packet.availability(	czmlStartTime,
											czmlStopTime);
		}

		final Polyline polyline = Polyline.create()
				.width(DoubleRefValue.number(properties.getWidth()))
				.clampToGround(BooleanRefValue.booleanValue(true))
				.material(PolylineMaterial.create()
						.polylineOutline(PolylineOutlineMaterial.create()
								.color(ColorRefValue.color(new Color(
										new BigInteger(
												properties.getColor(),
												16).intValue(),
										true)))
								.outlineColor(ColorRefValue.color(new Color(
										new BigInteger(
												properties.getOutlineColor(),
												16).intValue(),
										true)))
								.outlineWidth(DoubleRefValue.number(properties.getOutlineWidth()))))
				.positions(PositionList.geometry(geometry));

		return packet.polyline(polyline)
				.writeString();
	}

	public String asCsv() {
		final List<String> values = new ArrayList<>();
		values.add(targetId);
		values.add(targetName);
		values.add(description != null ? description : "");
		values.add(countryCode);
		values.add(geoRegion);
		values.add(orderOfBattle.toString());
		values.add(targetType.toString());
		values.add(new WKTWriter().write(geometry));

		return values.stream()
				.collect(Collectors.joining(", "));
	}
}
