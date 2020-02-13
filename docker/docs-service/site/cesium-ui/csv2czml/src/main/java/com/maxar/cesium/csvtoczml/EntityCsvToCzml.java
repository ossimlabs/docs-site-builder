package com.maxar.cesium.csvtoczml;

import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.label.Font;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.offset.PixelOffset;
import com.maxar.cesium.czmlwriter.origin.HorizontalOrigin;
import com.maxar.cesium.czmlwriter.packet.Billboard;
import com.maxar.cesium.czmlwriter.packet.Label;
import com.maxar.cesium.czmlwriter.packet.LayerControl;
import com.maxar.cesium.czmlwriter.packet.Model;
import com.maxar.cesium.czmlwriter.packet.Orientation;
import com.maxar.cesium.czmlwriter.packet.Path;
import com.maxar.cesium.czmlwriter.packet.Position;
import com.maxar.cesium.czmlwriter.refvalue.BooleanRefValue;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.cesium.czmlwriter.refvalue.StringRefValue;
import com.maxar.cesium.czmlwriter.refvalue.UriRefValue;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.LatLonAlt;

import cesiumlanguagewriter.CesiumHorizontalOrigin;
import cesiumlanguagewriter.CesiumResourceBehavior;
import cesiumlanguagewriter.PacketCesiumWriter;

public class EntityCsvToCzml
{
	public final static String ENTITY_PARENTS_NAME = "Entities";
	public final static String ENTITY_PARENTS_ID = ENTITY_PARENTS_NAME.toLowerCase();

	public final static String COUGHT_ENTITY_PARENTS_NAME = "Cought Entities";
	public final static String COUGHT_ENTITY_PARENTS_ID = "cought-entities";

	private static final String BILLBOARD_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACEAAAANCAYAAAAnigciAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAO+SURBVDhPvZRbTBx1FMZ/O3uZhV3YLculCy13VmjLvTQtEdIK1GijNiaNNfXBF2JsojWIL40m4Iu8mJBoLD60plFjE6smRSK1NDGWYJQAtjQUJbDcs1x2WRZ22cvsjv8lPPgiJcb4JTOTOZNzzne+8/2H/xpvXmpR60/Wqze//k7dCT0Wmp3nv8YPfT+r8/PzmPFy9fqX1O1zcf6N87R03KDn9uSe6muuXb2m9t3+ivEJF25PAI0UD4shRHo4FOLMs2dINJlESBXReE0VSdKxtDjOAfMaie5F0pNkSgoyWNQU8ES2ntlHD9EXFvD6Bz0sON2PJaJ5v+2y6pvsg1gYJRrFYDBsf4hEFBRF2Sai1emQtJJoLqETLPWyTIrNgiM7CWNU0AoGubMs4ZxUeOZULqWWMM7hByQ5cnix9Va8zq5E9iTX3/F262X1w86POGi30fhUDUX73OgsFpSYSpZJYmLFy5HsTDwuD3POGYbmFQbHPKx5Vrbz40JXVR9j9OEguZmp/OFc0eyZRPNrb6m/DXzOu++0cLwgwtTIFHKinnVdAh1f/MrzVXa8oQi/jLk5VZaKUdZxujKf0Z8GyCkvIU9csWiY3uFpzH4vxRlWLrTdZHTKp9Hu9PhHNDdfVGttrrbCEis1NfmsbsaYHnqAz7tFyO3DbUwUKgRJy0jlaEk26WlGxpd9rIc0RMwS+8WY7vk5csqKcS4s0T04xbH8DFaWF1mVSoQiY+27KtHZ+bHqdt7i+343mVYZv19iw7dEcrKVoH8DSSehCD8kJhgJB0PE0ArbRkgwyGJqYZYkE3ZdWKxAIScrA0NaKlqzhkOpRr65cYfqC+20Xrq4uxKlFRVtC9NT1B6yca4pD6uspagwDf/WFg2NtXjW/ZxsqBaLNlJXV0FACfNk/VFiwsQnjpezsaFw5EQ1a+EE9udmMfz7nxxI0jLsVLneO8KPvT3bIuxKYqD/Xvv9RzNt55rKiQR85JUUc/a5MvRSjMP2ECmpZl55oZKA301lToxkk5FXX6pBDQcoPRgkPUXm5bMid9NLVVGUfKFGVNkkGvRxb2Q23qI9ftv+K+yGrq4rvPdpP98OywyPTuCac6HXbFFWlo/NpGNmZnZ7CaWlBaJpAjPT0+iUoHh3kGYzMeucE6RDlB52kJGeRMdnQ/TdX+eTK107HfZ4RBsaG9S7fXdxOIqwWkz4NgKYZAP+kILFIrMpTKrVIzwTw2Yx4PL4kIVfImEVu93K0vImWk2YQARWVt08fbqJ7u7uPZ/M/wHwFyhxduZCESNOAAAAAElFTkSuQmCC";

	public static List<JsonNode> writeEntityCsv(
			final File file ) {
		final List<String[]> lines = CsvReader.readCsv(file);

		final Map<String, List<SimEntity>> entitys = lines.subList(	1,
																	lines.size())
				.stream()
				.map(tokens -> new SimEntity(
						tokens[0],
						tokens[1],
						Double.parseDouble(tokens[2]),
						Double.parseDouble(tokens[3])))
				.collect(Collectors.groupingBy(SimEntity::getId));

		final List<JsonNode> entities = entitys.entrySet()
				.stream()
				.map(entry -> writeEntity(	entry.getKey(),
											entry.getValue()))
				.collect(Collectors.toList());

		entities.add(	0,
						Packet.create()
								.id(ENTITY_PARENTS_ID)
								.name(ENTITY_PARENTS_NAME)
								.toJsonNode());
		return entities;
	}

	private static JsonNode writeEntity(
			final String entityName,
			final List<SimEntity> entityPositions ) {
		return Packet.create()
				.id(entityName)
				.name(entityName)
				.parent(ENTITY_PARENTS_ID)
				.path(Path.create()
						.leadTime(DoubleRefValue.number(0.0))
						.trailTime(DoubleRefValue.number(500.0))
						.material(PolylineMaterial.create()
								.polylineOutline(PolylineOutlineMaterial.create()
										.color(ColorRefValue.color(new Color(
												0,
												255,
												255,
												255)))
										.outlineWidth(DoubleRefValue.number(0.0)))))
				.position(Position.cartographicRadians(entityPositions.stream()
						.map(entity -> entity.asTimeTaggedLatLonAlt())
						.collect(Collectors.toList())))
				.model(Model.create()
						.uri(UriRefValue.uri(	"/assets/models/Cesium_Ground.glb",
												CesiumResourceBehavior.LINK_TO))
						.scale(DoubleRefValue.number(100.0)))
				.orientation(Orientation.velocityReference(	entityName,
															PacketCesiumWriter.PositionPropertyName))
				.toJsonNode();
	}

	public static List<JsonNode> writeEntitiesCaught(
			final File file,
			final DateTime end ) {

		final List<String[]> lines = CsvReader.readCsv(file);
		final List<JsonNode> packets = lines.subList(	1,
														lines.size())
				.stream()
				.map(tokens -> writeEntityCought(	tokens,
													end))
				.collect(Collectors.toList());

		packets.add(0,
					Packet.create()
							.id(COUGHT_ENTITY_PARENTS_ID)
							.name(COUGHT_ENTITY_PARENTS_NAME)
							.toJsonNode());

		return packets;
	}

	private static JsonNode writeEntityCought(
			final String[] tokens,
			final DateTime end ) {

		final DateTime startTime = DateTime.parse(tokens[1]);
		return Packet.create()
				.id(tokens[0] + tokens[1])
				.name(tokens[0])
				.parent(COUGHT_ENTITY_PARENTS_ID)
				.availability(	startTime,
								end)
				.billboard(Billboard.create()
						.image(UriRefValue.uri(	BILLBOARD_IMAGE,
												CesiumResourceBehavior.LINK_TO))
						.scale(DoubleRefValue.number(2.0)))
				.position(Position.cartographicRadians(new LatLonAlt(
						Angle.parse(tokens[2],
									"deg"),
						Angle.parse(tokens[3],
									"deg"),
						Length.Zero())))
				.label(Label.create()
						.font(Font.font("12pt Lucida Console"))
						.pixelOffset(PixelOffset.cartesian2(15,
															5))
						.text(StringRefValue.string(tokens[0]))
						.showBackground(BooleanRefValue.booleanValue(true))
						.backgroundColor(ColorRefValue.color(new Color(
								112,
								89,
								57,
								255)))
						.horizontalOrigin(HorizontalOrigin.horizontalOrigin(CesiumHorizontalOrigin.LEFT)))
				.layerControl(LayerControl.create()
						.zoomStart(startTime))
				.toJsonNode();
	}

}
