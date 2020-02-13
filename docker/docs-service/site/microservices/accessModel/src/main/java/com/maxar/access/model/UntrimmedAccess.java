package com.maxar.access.model;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTWriter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maxar.access.model.czml.AccessCzmlProperties;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.packet.LayerControl;
import com.maxar.cesium.czmlwriter.packet.Path;
import com.maxar.cesium.czmlwriter.packet.Polyline;
import com.maxar.cesium.czmlwriter.packet.Position;
import com.maxar.cesium.czmlwriter.packet.TimelineControl;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.refvalue.BooleanRefValue;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.EarthCenteredFrame;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.LatLonAlt;
import com.radiantblue.analytics.isr.core.model.asset.IAsset;

import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;
import lombok.Data;

@Data
public class UntrimmedAccess
{
	private static Logger logger = SourceLogger.getLogger(UntrimmedAccess.class.getName());

	private String startTimeISO8601;
	private String endTimeISO8601;
	private String tcaTimeISO8601;
	private String assetID;
	private String assetName;
	private String failureReason;
	private String sensorMode;
	private String sensorType;
	private int rev;
	private int pass;
	private List<Access> trimmedAccesses;
	private String propagatorType;
	private Long czmlAssetStartTimeBufferMs;
	private Long czmlAssetEndTimeBufferMs;

	@JsonIgnore
	private Geometry geometry;

	@JsonIgnore
	private IAsset asset;

	@JsonIgnore
	private List<JsonNode> czml;

	public String getAccessId() {
		return assetName + "-" + rev + "-" + pass + "-" + startTimeISO8601;
	}

	public String produceCzml(
			final AccessCzmlProperties properties ) {
		return produceCzml(	null,
							properties);
	}

	private Color getColorFromSensorType(
			final AccessCzmlProperties properties ) {
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
			final String parentPacketId,
			final AccessCzmlProperties properties ) {
		DateTime start;
		DateTime tca;
		DateTime end;
		try {
			start = ISODateTimeFormat.dateTimeParser()
					.parseDateTime(startTimeISO8601);
			tca = ISODateTimeFormat.dateTimeParser()
					.parseDateTime(tcaTimeISO8601);
			end = ISODateTimeFormat.dateTimeParser()
					.parseDateTime(endTimeISO8601);
		}
		catch (final Exception e) {
			logger.error("Cannot parse ISO8601 start, tca or end datetime String: " + startTimeISO8601 + "/"
					+ tcaTimeISO8601 + "/" + endTimeISO8601);
			return null;
		}

		final long assetStartTimeBufferMs = Optional.ofNullable(czmlAssetStartTimeBufferMs)
				.orElse(properties.getAssetStartTimeBufferMs());
		final long assetEndTimeBufferMs = Optional.ofNullable(czmlAssetEndTimeBufferMs)
				.orElse(properties.getAssetEndTimeBufferMs());

		final DateTime adjustedStart = dateTimeMin(	start,
													tca.minus(assetStartTimeBufferMs));
		final DateTime adjustedEnd = dateTimeMax(	end,
													tca.plus(assetEndTimeBufferMs));

		final List<TimeTaggedValue<LatLonAlt>> timedOrbitPositions = new ArrayList<>();
		final List<LatLonAlt> orbitPositions = new ArrayList<>();
		final List<TimeTaggedValue<LatLonAlt>> timedGroundPositions = new ArrayList<>();
		final List<LatLonAlt> groundPositions = new ArrayList<>();
		DateTime atTime = new DateTime(
				adjustedStart);
		while (atTime.isBefore(adjustedEnd)) {
			addPositionAtTimeToLists(	atTime,
										timedOrbitPositions,
										orbitPositions,
										timedGroundPositions,
										groundPositions);
			atTime = atTime.plus(properties.getSamplingMS());

			// Add the last moment of the timeframe as the final sample
			if (!atTime.isBefore(adjustedEnd)) {
				atTime = adjustedEnd;
				addPositionAtTimeToLists(	atTime,
											timedOrbitPositions,
											orbitPositions,
											timedGroundPositions,
											groundPositions);
			}
		}

		final StringBuilder czmlString = new StringBuilder();

		// determine color to use based on sensor type
		final Color sensorColor = getColorFromSensorType(properties);

		// create packet for entire access
		final String accessId = getAccessId();
		Packet accessPacket = Packet.create()
				.id(accessId)
				.name(accessId)
				.timelineControl(TimelineControl.create()
						.start(adjustedStart)
						.end(adjustedEnd)
						.group(assetName + " Accesses"))
				.layerControl(LayerControl.create()
						.zoomToChildId(accessId + "-Laser"))
				.availability(	adjustedStart,
								adjustedEnd);

		if (parentPacketId != null) {
			accessPacket = accessPacket.parent(parentPacketId);
		}

		czmlString.append(accessPacket.writeString())
				.append(",");

		// create packet for target
		Geometry targetGeometry = geometry;
		final Point center = targetGeometry.getCentroid();
		if (geometry instanceof Point) {
			targetGeometry = geometry.buffer(0.001);
		}
		final LatLonAlt targetPosition = new LatLonAlt(
				Angle.fromDegrees(center.getY()),
				Angle.fromDegrees(center.getX()),
				Length.Zero());
		final Polyline polyline = Polyline.create()
				.width(DoubleRefValue.number(properties.getWidth()))
				.clampToGround(BooleanRefValue.booleanValue(true))
				.material(PolylineMaterial.create()
						.polylineOutline(PolylineOutlineMaterial.create()
								.color(ColorRefValue.color(sensorColor))
								.outlineColor(ColorRefValue.color(new Color(
										new BigInteger(
												properties.getOutlineColor(),
												16).intValue(),
										true)))
								.outlineWidth(DoubleRefValue.number(properties.getOutlineWidth()))))
				.positions(PositionList.geometry(targetGeometry));

		final Packet targetPacket = Packet.create()
				.id(accessId + "-Target")
				.name(accessId + "-Target")
				.parent(accessId)
				.layerControl(LayerControl.create()
						.showAsLayer(properties.isDisplayAccessInTree()))
				.polyline(polyline)
				.position(Position.cartographicRadians(targetPosition));

		czmlString.append(targetPacket.writeString())
				.append(",");

		// create packet for asset positions
		final Packet assetPacket = Packet.create()
				.id(accessId + "-Asset")
				.name(accessId + "-Asset")
				.parent(accessId)
				.layerControl(LayerControl.create()
						.showAsLayer(properties.isDisplayAccessInTree()))
				.path(Path.create()
						.leadTime(DoubleRefValue.number(0.0))
						.trailTime(DoubleRefValue.number(500.0))
						.material(PolylineMaterial.create()
								.polylineOutline(PolylineOutlineMaterial.create()
										.color(ColorRefValue.color(sensorColor))
										.outlineWidth(DoubleRefValue.number(properties.getOutlineWidth())))))
				.position(Position.cartographicRadians(timedOrbitPositions));

		czmlString.append(assetPacket.writeString())
				.append(",");

		if (properties.isDisplayAssetGroundTrace()) {
			// create packet for asset ground trace positions
			final Packet assetGroundTracePacket = Packet.create()
					.id(accessId + "-AssetGroundTrace")
					.name(accessId + "-AssetGroundTrace")
					.parent(accessId)
					.layerControl(LayerControl.create()
							.showAsLayer(properties.isDisplayAccessInTree()))
					.path(Path.create()
							.leadTime(DoubleRefValue.number(0.0))
							.trailTime(DoubleRefValue.number(500.0))
							.material(PolylineMaterial.create()
									.polylineOutline(PolylineOutlineMaterial.create()
											.color(ColorRefValue.color(sensorColor))
											.outlineWidth(DoubleRefValue.number(properties.getOutlineWidth())))))
					.position(Position.cartographicRadians(timedGroundPositions));

			czmlString.append(assetGroundTracePacket.writeString())
					.append(",");
		}

		// create packet for line between asset positions and target
		final Packet laserPacket = Packet.create()
				.id(accessId + "-Laser")
				.name(accessId + "-Laser")
				.parent(accessId)
				.layerControl(LayerControl.create()
						.showAsLayer(properties.isDisplayAccessInTree()))
				.availability(	start,
								end)
				.polyline(Polyline.create()
						.material(PolylineMaterial.create()
								.polylineOutline(PolylineOutlineMaterial.create()
										.color(ColorRefValue.color(sensorColor))
										.outlineWidth(DoubleRefValue.number(properties.getOutlineWidth()))))
						.positions(PositionList.references(Arrays.asList(	new Reference(
								accessId + "-Asset",
								PacketCesiumWriter.PositionPropertyName),
																			new Reference(
																					accessId + "-Target",
																					PacketCesiumWriter.PositionPropertyName)))));

		czmlString.append(laserPacket.writeString())
				.append(",");

		// create static orbit and lines for rise/set/tca
		final String staticAccessId = accessId + "-Static";
		final Packet staticAccessPacket = Packet.create()
				.id(staticAccessId)
				.name(staticAccessId)
				.parent(accessId)
				.layerControl(LayerControl.create()
						// Parent has to be visible as well for tree to render correctly
						.showAsLayer(properties.isDisplayAccessInTree() && properties.isDisplayStaticAccessInTree()));

		czmlString.append(staticAccessPacket.writeString())
				.append(",");

		final Polyline staticTargetPolyline = Polyline.create()
				.width(DoubleRefValue.number(properties.getWidth()))
				.clampToGround(BooleanRefValue.booleanValue(true))
				.material(PolylineMaterial.create()
						.polylineOutline(PolylineOutlineMaterial.create()
								.color(ColorRefValue.color(sensorColor))
								.outlineColor(ColorRefValue.color(new Color(
										new BigInteger(
												properties.getStaticOutlineColor(),
												16).intValue(),
										true)))
								.outlineWidth(DoubleRefValue.number(properties.getStaticOutlineWidth()))))
				.positions(PositionList.geometry(targetGeometry));

		// Create static target position
		final Packet staticTargetPacket = Packet.create()
				.id(staticAccessId + "-Target")
				.name(staticAccessId + "-Target")
				.parent(staticAccessId)
				.layerControl(LayerControl.create()
						.showAsLayer(properties.isDisplayAccessInTree() && properties.isDisplayStaticAccessInTree()))
				.polyline(staticTargetPolyline)
				.position(Position.cartographicRadians(new LatLonAlt(
						Angle.fromDegrees(center.getY()),
						Angle.fromDegrees(center.getX()),
						Length.Zero())));

		czmlString.append(staticTargetPacket.writeString())
				.append(",");

		// create packet for asset positions
		final Polyline staticAssetPolyline = Polyline.create()
				.width(DoubleRefValue.number(properties.getWidth()))
				.clampToGround(BooleanRefValue.booleanValue(false))
				.material(PolylineMaterial.create()
						.polylineOutline(PolylineOutlineMaterial.create()
								.color(ColorRefValue.color(new Color(
										new BigInteger(
												properties.getStaticColor(),
												16).intValue(),
										true)))
								.outlineColor(ColorRefValue.color(new Color(
										new BigInteger(
												properties.getStaticOutlineColor(),
												16).intValue(),
										true)))
								.outlineWidth(DoubleRefValue.number(properties.getStaticOutlineWidth()))))
				.positions(PositionList.cartographicRadians(orbitPositions));

		final Packet staticAssetPacket = Packet.create()
				.id(staticAccessId + "-Asset")
				.name(staticAccessId + "-Asset")
				.parent(staticAccessId)
				.layerControl(LayerControl.create()
						.showAsLayer(properties.isDisplayAccessInTree() && properties.isDisplayStaticAccessInTree()))
				.polyline(staticAssetPolyline);

		czmlString.append(staticAssetPacket.writeString())
				.append(",");

		if (properties.isDisplayAssetGroundTrace()) {
			// create packet for asset ground trace positions
			final Polyline staticAssetGroundTracePolyline = Polyline.create()
					.width(DoubleRefValue.number(properties.getWidth()))
					.clampToGround(BooleanRefValue.booleanValue(false))
					.material(PolylineMaterial.create()
							.polylineOutline(PolylineOutlineMaterial.create()
									.color(ColorRefValue.color(new Color(
											new BigInteger(
													properties.getStaticColor(),
													16).intValue(),
											true)))
									.outlineColor(ColorRefValue.color(new Color(
											new BigInteger(
													properties.getStaticOutlineColor(),
													16).intValue(),
											true)))
									.outlineWidth(DoubleRefValue.number(properties.getStaticOutlineWidth()))))
					.positions(PositionList.cartographicRadians(groundPositions));

			final Packet staticAssetGroundTracePacket = Packet.create()
					.id(staticAccessId + "-AssetGroundTrace")
					.name(staticAccessId + "-AssetGroundTrace")
					.parent(staticAccessId)
					.layerControl(LayerControl.create()
							.showAsLayer(properties.isDisplayAccessInTree()
									&& properties.isDisplayStaticAccessInTree()))
					.polyline(staticAssetGroundTracePolyline);

			czmlString.append(staticAssetGroundTracePacket.writeString())
					.append(",");
		}

		// create packets for "lasers"

		// rise
		final Packet staticRisePacket = getLaser(	properties,
													staticAccessId,
													start,
													"Rise",
													targetPosition,
													sensorColor);

		czmlString.append(staticRisePacket.writeString())
				.append(",");

		// tca
		final Packet staticTcaPacket = getLaser(properties,
												staticAccessId,
												tca,
												"TCA",
												targetPosition,
												sensorColor);

		czmlString.append(staticTcaPacket.writeString())
				.append(",");

		// set
		final Packet staticSetPacket = getLaser(properties,
												staticAccessId,
												end,
												"Set",
												targetPosition,
												sensorColor);

		czmlString.append(staticSetPacket.writeString());

		return czmlString.toString();
	}

	private Packet getLaser(
			final AccessCzmlProperties props,
			final String parentId,
			final DateTime atTime,
			final String label,
			final LatLonAlt targetPosition,
			final Color sensorColor ) {
		final GeodeticPoint assetPosition = asset.getStateVectors(	atTime,
																	EarthCenteredFrame.ECEF)
				.geodeticPosition();
		final LatLonAlt assetPositionLla = new LatLonAlt(
				assetPosition.latitude(),
				assetPosition.longitude(),
				assetPosition.altitude());
		final List<LatLonAlt> positions = new ArrayList<>();
		positions.add(assetPositionLla);
		positions.add(targetPosition);
		final Polyline polyline = Polyline.create()
				.width(DoubleRefValue.number(props.getWidth()))
				.clampToGround(BooleanRefValue.booleanValue(false))
				.material(PolylineMaterial.create()
						.polylineOutline(PolylineOutlineMaterial.create()
								.color(ColorRefValue.color(sensorColor))
								.outlineColor(ColorRefValue.color(new Color(
										new BigInteger(
												props.getStaticOutlineColor(),
												16).intValue(),
										true)))
								.outlineWidth(DoubleRefValue.number(props.getStaticOutlineWidth()))))
				.positions(PositionList.cartographicRadians(positions));

		return Packet.create()
				.id(parentId + "-" + label)
				.name(parentId + "-" + label)
				.parent(parentId)
				.layerControl(LayerControl.create()
						.showAsLayer(props.isDisplayAccessInTree() && props.isDisplayStaticAccessInTree()))
				.polyline(polyline);
	}

	public List<String> asCsv() {

		final List<String> values = new ArrayList<>();
		values.add(startTimeISO8601);
		values.add(tcaTimeISO8601);
		values.add(endTimeISO8601);
		values.add(assetID);
		values.add(sensorMode);
		values.add(failureReason != null ? failureReason : "");
		values.add(Integer.toString(rev));
		values.add(Integer.toString(pass));
		values.add(propagatorType);
		values.add(new WKTWriter().write(geometry));

		final List<List<String>> trimmedAccessesLines = trimmedAccesses.stream()
				.map(access -> {
					final List<String> trimmedAccess = new ArrayList<>(
							Collections.nCopies(10,
												""));
					trimmedAccess.add(access.getStartTimeISO8601());
					trimmedAccess.add(access.getTcaTimeISO8601());
					trimmedAccess.add(access.getEndTimeISO8601());
					return trimmedAccess;
				})
				.collect(Collectors.toList());

		trimmedAccessesLines.add(	0,
									values);

		return trimmedAccessesLines.stream()
				.map(line -> String.join(	", ",
											line))
				.collect(Collectors.toList());
	}

	public List<JsonNode> getCzml() {
		for (JsonNode czmlPacket : this.czml) {
			final JsonNode id = czmlPacket.get("id");
			if (id.textValue()
					.equals("Space Accesses")
					|| id.textValue()
							.equals("Airborne Accesses")) {
				((ObjectNode) czmlPacket).put(	"parent",
												"Accesses");
				break;
			}
		}

		return this.czml;
	}

	private void addPositionAtTimeToLists(
			final DateTime atTime,
			final List<TimeTaggedValue<LatLonAlt>> timedOrbitPositions,
			final List<LatLonAlt> orbitPositions,
			final List<TimeTaggedValue<LatLonAlt>> timedGroundPositions,
			final List<LatLonAlt> groundPositions ) {
		final GeodeticPoint orbitPosition = asset.getStateVectors(	atTime,
																	EarthCenteredFrame.ECEF)
				.geodeticPosition();

		addGeodeticPositionAtTimeToLists(	orbitPosition,
											atTime,
											timedOrbitPositions,
											orbitPositions);

		final GeodeticPoint groundPosition = orbitPosition.withZeroAltitude();

		addGeodeticPositionAtTimeToLists(	groundPosition,
											atTime,
											timedGroundPositions,
											groundPositions);
	}

	private static void addGeodeticPositionAtTimeToLists(
			final GeodeticPoint position,
			final DateTime atTime,
			final List<TimeTaggedValue<LatLonAlt>> timedPositions,
			final List<LatLonAlt> positions ) {
		final LatLonAlt lla = new LatLonAlt(
				position.latitude(),
				position.longitude(),
				position.altitude());

		timedPositions.add(new TimeTaggedValue<>(
				atTime,
				lla));

		positions.add(lla);
	}

	private static DateTime dateTimeMin(
			final DateTime dateTime0,
			final DateTime dateTime1 ) {
		if (dateTime0.isBefore(dateTime1)) {
			return dateTime0;
		}

		return dateTime1;
	}

	private static DateTime dateTimeMax(
			final DateTime dateTime0,
			final DateTime dateTime1 ) {
		if (dateTime0.isAfter(dateTime1)) {
			return dateTime0;
		}

		return dateTime1;
	}
}
