package com.maxar.weather.parser;

import java.io.InputStream;
import java.util.List;

public abstract class DataTypeParser<DataType>
{
	private String metaData;

	public List<DataType> parseData(
			final InputStream input )
					throws Exception {
		return parseData(
				input,
				0,
				Integer.MAX_VALUE);
	}

	public List<DataType> parseData(
			final InputStream input,
			final String dataString )
					throws Exception {

		setMetaData(
				dataString);

		return parseData(
				input,
				0,
				Integer.MAX_VALUE);
	}

	public int countData(
			final InputStream input ) {
		return 0;
	}

	public abstract List<DataType> parseData(
			InputStream input,
			int beginIndex,
			int toIndex )
					throws Exception;

	public String getMetaData() {
		return metaData;
	}

	public void setMetaData(
			final String metaData ) {
		this.metaData = metaData;
	}

	/**
	 * Will be used to track status, this is the total items that were parsed
	 *
	 * @param numberOfSets
	 *            Number of sets in the file to process
	 *
	 * @return The total number of items parsed.
	 */
	public Integer totalToParse(
			final int numberOfSets ) {
		return 1;
	}
}
