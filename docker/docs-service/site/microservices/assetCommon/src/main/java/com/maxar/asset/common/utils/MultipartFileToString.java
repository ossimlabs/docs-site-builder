package com.maxar.asset.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.web.multipart.MultipartFile;

public class MultipartFileToString
{
	/**
	 * Read from a multipart file into a string.
	 *
	 * @param file The input file.
	 * @return A string containing the contents of the file, with each line separated by a line feed (\n).
	 * @throws IOException Thrown if there is a problem reading from the file.
	 */
	public static String multipartFileToString(final MultipartFile file) throws
			IOException {
		try (final InputStream inputStream = file.getInputStream()) {
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			final StringBuilder stringBuilder = new StringBuilder();

			bufferedReader.lines().forEach(line -> stringBuilder.append(line).append(System.lineSeparator()));

			return stringBuilder.toString();
		}
	}
}
