package com.maxar.cesium.csvtoczml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvReader
{
	public static List<String[]> readCsv(
			final File file ) {

		final List<String[]> lines = new ArrayList<>();

		try (final BufferedReader reader = new BufferedReader(
				new FileReader(
						file))) {

			while (reader.ready()) {
				final String line = reader.readLine();

				final String[] tokens = line.split(",");

				lines.add(tokens);
			}

		}
		catch (final IOException e) {
			System.out.println("Failed to read csv:" + file);
		}

		return lines;
	}

}
