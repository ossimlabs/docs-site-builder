package com.maxar.cesium.csvtoczml;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.packet.Polyline;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.refvalue.BooleanRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;

public class RequirementsCsvToCzml
{
	private static final String REQS_PARENT_NAME = "Requirements";
	private static final String REQS_PARENT_ID = REQS_PARENT_NAME.toLowerCase();

	public static List<JsonNode> writeRequirementsCsv(
			final File file ) {

		final List<String[]> lines = CsvReader.readCsv(file);

		final List<JsonNode> reqsCzml = lines.subList(1,
														lines.size())
				.stream()
				.map(RequirementsCsvToCzml::tokensToJson)
				.collect(Collectors.toList());

		reqsCzml.add(	0,
						Packet.create()
								.id(REQS_PARENT_ID)
								.name(REQS_PARENT_NAME)
								.toJsonNode());

		return reqsCzml;
	}

	private static JsonNode tokensToJson(
			final String[] tokens ) {

		Packet packet = Packet.create()
				.id(tokens[0])
				.name(tokens[0])
				.parent(REQS_PARENT_ID);

		Polyline polyline = Polyline.create()
				.clampToGround(BooleanRefValue.booleanValue(true))
				.material(PolylineMaterial.create()
						.polylineOutline(PolylineOutlineMaterial.create()
								.outlineWidth(DoubleRefValue.number(0.0))))
				.positions(PositionList.cartographicRadians(CsvToCzmlWriter.readLatLonList(	tokens,
																							9)));

		return packet.polyline(polyline)
				.toJsonNode();
	}
}
