package com.maxar.cesium.server.czmlwriter;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxar.cesium.csvtoczml.CsvToCzmlWriter;

public class CsvWriterTest
{

	final static String ip = "localhost:8081";
	//final static String ip = "10.0.0.188:8081";
	final static String session = "NewSim";

	public static void main(
			final String[] args ) {
		testSimCsvs();
	}

	public static void testSimCsvs() {

		final String folderName = ".\\src\\test\\resources\\testcsv";
		//final String folderName = "c:\\output";

		final List<JsonNode> array = CsvToCzmlWriter.writeCsvFolderToCzml(folderName);

		CzmlTestUtils.sendPackets(	array,
									true,
									ip,
									session);
	}
}
