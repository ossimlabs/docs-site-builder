package com.maxar.cesium.csvtoczml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.LatLonAlt;

public class CsvToCzmlWriter
{
	private static String TIME_FRAME_FILE_NAME = "timeframe.csv";
	private static String POSITION_FILE_NAME_END = "_pos.csv";
	private static String OPS_FILE_NAME_END = "_ops.csv";
	private static String ENTITY_FILE_NAME = "entities.csv";
	private static String ENTITY_CAUGHT_FILE_NAME = "entities_caught.csv";
	private static String REQUIREMENTS_FILE_NAME = "requirements.csv";

	public static List<JsonNode> writeCsvFolderToCzml(
			final String folderName ) {

		final File dir = new File(
				folderName);
		
		final List<JsonNode> array = new ArrayList<>();

		final File timeFrameCsv = new File(
				dir,
				TIME_FRAME_FILE_NAME);

		if (!timeFrameCsv.exists()) {
			return array;
		}

		final String[] tokens = CsvReader.readCsv(timeFrameCsv)
				.get(1);

		final DateTime end = DateTime.parse(tokens[1]);

		array.add(AssetCsvToCzml.getAssetParentPacket());

		// ASSETS
		final List<File> files = Arrays.asList(dir.listFiles());
		final Stream<File> positionFiles = files.stream()
				.filter(file -> file.getName()
						.contains(POSITION_FILE_NAME_END));
		final Stream<JsonNode> positionObjects = positionFiles.flatMap(posFile -> AssetCsvToCzml.writeAssetCsv(posFile)
				.stream());
		addStreamToArray(	positionObjects,
							array);

		// OPS
		final Stream<File> opsFiles = files.stream()
				.filter(file -> file.getName()
						.contains(OPS_FILE_NAME_END));
		final Stream<JsonNode> opsObjects = opsFiles.flatMap(opsFile -> AssetCsvToCzml.writeOpsCsv(	opsFile,
																									end)
				.stream());
		addStreamToArray(	opsObjects,
							array);

		// ENTITYS
		final File entityFile = new File(
				dir,
				ENTITY_FILE_NAME);
		if (entityFile.exists()) {
			final Stream<JsonNode> entityObjects = EntityCsvToCzml.writeEntityCsv(entityFile)
					.stream();
			addStreamToArray(	entityObjects,
								array);
		}

		// ENTITIES CAUGHT
		final File entityCaughtFile = new File(
				dir,
				ENTITY_CAUGHT_FILE_NAME);
		if (entityCaughtFile.exists()) {
			final Stream<JsonNode> entitiesCoughtObjects = EntityCsvToCzml.writeEntitiesCaught(	entityCaughtFile,
																								end)
					.stream();
			addStreamToArray(	entitiesCoughtObjects,
								array);
		}

		// REQUIREMENTS
		final File requirementsFile = new File(
				dir,
				REQUIREMENTS_FILE_NAME);
		if (requirementsFile.exists()) {
			final Stream<JsonNode> requirementObjects = RequirementsCsvToCzml.writeRequirementsCsv(requirementsFile)
					.stream();
			addStreamToArray(	requirementObjects,
								array);
		}

		return array;

	}

	private static void addStreamToArray(
			final Stream<JsonNode> stream,
			final List<JsonNode> array ) {
		stream.forEach(JsonNode -> array.add(JsonNode));
	}

	public static List<LatLonAlt> readLatLonList(
			final String[] tokens,
			final int startIndex ) {

		final List<LatLonAlt> latLons = new ArrayList<>();

		for (int i = startIndex; i < tokens.length; i = i + 2) {
			latLons.add(new LatLonAlt(
					Angle.parse(tokens[i],
								"deg"),
					Angle.parse(tokens[i + 1],
								"deg"),
					Length.Zero()));
		}

		return latLons;
	}
}
