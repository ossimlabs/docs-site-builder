package com.maxar.cesium.csvtoczml;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.packet.LayerControl;
import com.maxar.cesium.czmlwriter.packet.Model;
import com.maxar.cesium.czmlwriter.packet.Orientation;
import com.maxar.cesium.czmlwriter.packet.Path;
import com.maxar.cesium.czmlwriter.packet.Polyline;
import com.maxar.cesium.czmlwriter.packet.Position;
import com.maxar.cesium.czmlwriter.packet.TimelineControl;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.refvalue.BooleanRefValue;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.cesium.czmlwriter.refvalue.UriRefValue;
import com.maxar.cesium.czmlwriter.types.PropertyInterval;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.radiantblue.analytics.core.Quaternion;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.LatLonAlt;

import cesiumlanguagewriter.CesiumResourceBehavior;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.Reference;

public class AssetCsvToCzml
{
	public final static String ASSET_PARENTS_NAME = "Assets";
	public final static String ASSET_PARENTS_ID = ASSET_PARENTS_NAME.toLowerCase();

	public static JsonNode getAssetParentPacket() {
		return Packet.create()
				.id(ASSET_PARENTS_ID)
				.name(ASSET_PARENTS_NAME)
				.toJsonNode();
	}

	public static List<JsonNode> writeAssetCsv(
			final File file ) {

		final String fileName = file.getName();
		final int nameEndIndex = fileName.indexOf("_pos");
		final String name = fileName.substring(	0,
												nameEndIndex);

		final List<TimeTaggedValue<LatLonAlt>> positions = new ArrayList<>();

		final List<String[]> lines = CsvReader.readCsv(file);
		for (int i = 1; i < lines.size(); i++) {
			final String[] tokens = lines.get(i);

			final TimeTaggedValue<LatLonAlt> latLonAlt = new TimeTaggedValue<>(
					DateTime.parse(tokens[1]),
					new LatLonAlt(
							Angle.parse(tokens[2],
										"deg"),
							Angle.parse(tokens[3],
										"deg"),
							Length.parse(	tokens[4],
											"m")));
			positions.add(latLonAlt);
		}

		final File atittudeFile = new File(
				file.getParent(),
				name + "_attitude.csv");

		List<TimeTaggedValue<Quaternion>> orientation = createOrientationFromAttitudeFile(atittudeFile);

		final String model;
		if (orientation != null) {
			model = "/assets/models/LowPoly_Satellite.gltf";
		}
		else {

			model = "/assets/models/CesiumDrone.gltf";
		}

		Packet orbitTrace = Packet.create()
				.id(name)
				.name(name)
				.parent(ASSET_PARENTS_ID)
				.path(Path.create()
						.leadTime(DoubleRefValue.number(0.0))
						.trailTime(DoubleRefValue.number(500.0))
						.material(PolylineMaterial.create()
								.polylineOutline(PolylineOutlineMaterial.create()
										.outlineWidth(DoubleRefValue.number(0.0)))))
				.position(Position.cartographicRadians(positions))
				.model(Model.create()
						.uri(UriRefValue.uri(	model,
												CesiumResourceBehavior.LINK_TO))
						.scale(DoubleRefValue.number(100.0)));

		if (orientation != null) {
			orbitTrace = orbitTrace.orientation(Orientation.quaternion(orientation));
		}
		else {
			orbitTrace = orbitTrace.orientation(Orientation.velocityReference(	name,
																				PacketCesiumWriter.PositionPropertyName));
		}

		final List<TimeTaggedValue<LatLonAlt>> groundTracePositions = positions.stream()
				.map(latLonAlt -> {
					return new TimeTaggedValue<LatLonAlt>(
							latLonAlt.getTime(),
							new LatLonAlt(
									latLonAlt.getValue()
											.latitude(),
									latLonAlt.getValue()
											.longitude(),
									Length.Zero()));
				})
				.collect(Collectors.toList());

		final JsonNode groundTrace = Packet.create()
				.id(name + "-groundTrace")
				.name(name + " Ground Trace")
				.parent(name)
				.layerControl(LayerControl.create()
						.showAsLayer(false))
				.path(Path.create()
						.leadTime(DoubleRefValue.number(0.0))
						.trailTime(DoubleRefValue.number(500.0))
						.material(PolylineMaterial.create()
								.polylineOutline(PolylineOutlineMaterial.create()
										.color(ColorRefValue.color(new Color(
												128,
												128,
												128,
												255)))
										.outlineWidth(DoubleRefValue.number(0.0)))))
				.position(Position.cartographicRadians(groundTracePositions))
				.toJsonNode();

		return Arrays.asList(	orbitTrace.toJsonNode(),
								groundTrace);
	}

	private static List<TimeTaggedValue<Quaternion>> createOrientationFromAttitudeFile(
			final File atittudeFile ) {
		if (atittudeFile.exists()) {
			final List<String[]> lines = CsvReader.readCsv(atittudeFile);

			if (lines.size() > 1) {

				final List<TimeTaggedValue<Quaternion>> quaternions = lines.subList(1,
																					lines.size())
						.stream()
						.map(tokens -> new TimeTaggedValue<Quaternion>(
								DateTime.parse(tokens[1]),
								Quaternion.createFromWXYZ(	Double.parseDouble(tokens[11]),
															Double.parseDouble(tokens[8]),
															Double.parseDouble(tokens[9]),
															Double.parseDouble(tokens[10]))))

						.collect(Collectors.toList());

				return quaternions;
			}
		}

		return null;
	}

	public static List<JsonNode> writeOpsCsv(
			final File file,
			final DateTime simEnd ) {

		final List<String[]> lines = CsvReader.readCsv(file);

		final String fileName = file.getName();
		final int nameEndIndex = fileName.indexOf("_ops");
		final String assetName = fileName.substring(0,
													nameEndIndex);
		int opNumber = 0;

		final List<JsonNode> jsonObjects = new ArrayList<>();

		for (int i = 1; i < lines.size(); i++) {
			final int currentOpNumber = opNumber++;

			final String opParentId = assetName + "-" + currentOpNumber;

			final String[] tokens = lines.get(i);
			final DateTime opStart = DateTime.parse(tokens[2]);
			final DateTime opEnd = DateTime.parse(tokens[3]);

			final List<LatLonAlt> points = CsvToCzmlWriter.readLatLonList(	tokens,
																			9);
			final DateTime finishedEnd = simEnd.isAfter(opEnd) ? simEnd : opEnd.plusMinutes(30);

			final String opName = assetName + " OP " + currentOpNumber;

			final ObjectNode opObject = Packet.create()
					.id(opParentId)
					.name(opName)
					.parent(assetName)
					.timelineControl(TimelineControl.create()
							.start(opStart)
							.end(opEnd)
							.group(assetName))
					.availability(	opStart,
									finishedEnd)
					.polyline(Polyline.create()
							.clampToGround(BooleanRefValue.booleanValue(true))
							.material(PolylineMaterial.create()
									.polylineOutline(PolylineOutlineMaterial.create()
											.color(Property.interval(Arrays.asList(	new PropertyInterval<ColorRefValue>(
													new Interval(
															opStart,
															opEnd),
													ColorRefValue.color(new Color(
															255,
															165,
															0,
															255))),
																					new PropertyInterval<ColorRefValue>(
																							new Interval(
																									opEnd,
																									finishedEnd),
																							ColorRefValue
																									.color(new Color(
																											255,
																											0,
																											0,
																											255))))))
											.outlineWidth(DoubleRefValue.number(0.0))))
							.positions(PositionList.cartographicRadians(points)))
					.position(Position.cartographicRadians(new LatLonAlt(
							Angle.parse(tokens[7],
										"deg"),
							Angle.parse(tokens[8],
										"deg"),
							Length.Zero())))
					.toJsonNode();
			jsonObjects.add(opObject);

			final JsonNode lazer = Packet.create()
					.id(assetName + "-" + currentOpNumber + "-lazer")
					.name(opName)
					.parent(opParentId)
					.availability(	opStart,
									opEnd)
					.layerControl(LayerControl.create()
							.layerName(opName + " lazer"))
					.polyline(Polyline.create()
							.material(PolylineMaterial.create()
									.polylineOutline(PolylineOutlineMaterial.create()
											.color(ColorRefValue.color(new Color(
													0,
													255,
													0,
													255)))
											.outlineWidth(DoubleRefValue.number(0.0))))
							.positions(PositionList.references(Arrays.asList(	new Reference(
									assetName,
									PacketCesiumWriter.PositionPropertyName),
																				new Reference(
																						opParentId,
																						PacketCesiumWriter.PositionPropertyName))))
							.followSurface(BooleanRefValue.booleanValue(false)))
					.toJsonNode();
			jsonObjects.add(lazer);
		}

		return jsonObjects;
	}
}
