package com.maxar.opgen.model;

import java.awt.Color;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.packet.Polyline;
import com.maxar.cesium.czmlwriter.packet.TimelineControl;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.refvalue.BooleanRefValue;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.cesium.czmlwriter.types.PropertyInterval;
import com.maxar.opgen.model.czml.OpCzmlProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Op
{
	String assetName;
	String sensorType;

	DateTime startTime;
	DateTime endTime;

	boolean isValid;
	String reason;

	List<OpBeam> beams;

	public String produceCzml() {
		return produceCzml(	null,
							new OpCzmlProperties());
	}

	public String produceCzml(
			final OpCzmlProperties properties ) {
		return produceCzml(	null,
							properties);
	}

	private Color getColorFromSensorType(
			final OpCzmlProperties properties ) {
		Color color = new Color(
				new BigInteger(
						properties.getColor(),
						16).intValue(),
				true);
		if (sensorType.equalsIgnoreCase("EO")) {
			color = new Color(
					new BigInteger(
							properties.getEoColor(),
							16).intValue(),
					true);
		}
		else if (sensorType.equalsIgnoreCase("RADAR")) {
			color = new Color(
					new BigInteger(
							properties.getRadarColor(),
							16).intValue(),
					true);
		}
		else if (sensorType.equalsIgnoreCase("IR")) {
			color = new Color(
					new BigInteger(
							properties.getIrColor(),
							16).intValue(),
					true);
		}
		return color;
	}

	public String produceCzml(
			final String parentId,
			final OpCzmlProperties properties ) {
		if (!isValid || beams.isEmpty()) {
			return "";
		}

		final String opParentId = UUID.randomUUID()
				.toString();
		Packet parentPacket = Packet.create()
				.id(opParentId)
				.name("ModeOp " + startTime.toString())
				.timelineControl(TimelineControl.create()
						.start(startTime)
						.end(endTime)
						.group(assetName + ":" + sensorType + " Ops"))
				.availability(	startTime,
								endTime);
		if (parentId != null) {
			parentPacket = parentPacket.parent(parentId);
		}

		final StringBuilder czmlString = new StringBuilder();
		czmlString.append(parentPacket.writeString());

		final WKTReader reader = new WKTReader();
		Geometry geometry = null;
		int beamId = 1;
		for (final OpBeam beam : beams) {
			final String id = UUID.randomUUID()
					.toString();

			final Packet packet = Packet.create()
					.id(id)
					.name("Beam " + (beamId++) + ": " + beam.getStartTime()
							.toString())
					.parent(opParentId)
					.availability(	beam.getStartTime(),
									beam.getEndTime()
											.plusSeconds(properties.getDisplayDurationSec()));

			try {
				geometry = reader.read(beam.getGeometryWkt());
			}
			catch (final ParseException e) {
				e.printStackTrace();
			}

			final Color opColor = getColorFromSensorType(properties);

			final Polyline polyline = Polyline.create()
					.width(DoubleRefValue.number(properties.getWidth()))
					.clampToGround(BooleanRefValue.booleanValue(true))
					.material(PolylineMaterial.create()
							.polylineOutline(PolylineOutlineMaterial.create()
									.color(ColorRefValue.color(opColor))
									.outlineColor(ColorRefValue.color(new Color(
											new BigInteger(
													properties.getOutlineColor(),
													16).intValue(),
											true)))
									.outlineWidth(DoubleRefValue.number(properties.getOutlineWidth()))))
					.positions(Property.interval(Arrays.asList(new PropertyInterval<PositionList>(
							new Interval(
									beam.getStartTime(),
									beam.getEndTime()
											.plusSeconds(properties.getDisplayDurationSec())),
							PositionList.geometry(geometry)))));

			czmlString.append("," + packet.polyline(polyline)
					.writeString());
		}

		return czmlString.toString();
	}
}
