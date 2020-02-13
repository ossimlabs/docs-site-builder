package com.maxar.weather.dm.dataingest;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;

import com.maxar.manager.dataingest.FileIngester;
import com.maxar.weather.parser.AbstractWeatherParser;
import com.maxar.weather.entity.map.RTNEPHQuarterGrid;
import com.maxar.weather.entity.map.WTM;
import com.maxar.weather.entity.weather.WeatherSet;
import com.maxar.weather.repository.WeatherSetRepository;
import com.radiantblue.analytics.core.log.SourceLogger;

import lombok.Getter;
import lombok.Setter;

public class WeatherFileIngester extends
		FileIngester
{
	private static Logger logger = SourceLogger.getLogger(WeatherFileIngester.class.getName());

	@Getter
	@Setter
	private AbstractWeatherParser<?> parser;

	@Getter
	@Setter
	private WeatherSetRepository weatherSetRepository;

	@Override
	public void ingestToDatabase(
			final File f,
			final List<File> associatedFiles )
			throws Exception {
		try (final InputStream input = openInputStream(f)) {
			logger.info("Ingesting weather from file: " + f.getName());

			final List<WeatherSet> wx = parser.parseData(	input,
															f.getName());

			// create or update db
			for (final WeatherSet ws : wx) {
				final long records = weatherSetRepository.deleteByAtTimeMillis(ws.getAtTimeMillis());

				logger.debug("Deleted " + records + " existing weather sets");

				weatherSetRepository.saveAndFlush(ws);
			}
		}

	}

	@Override
	protected void internalInit() {
		// successDir and errorDir are loaded by spring bypassing loadProperties
		// make sure they exist
		if (!successDir.isDirectory() && !successDir.mkdir()) {
			logger.warn("Unable to create success directory, " + successDir.getAbsolutePath());
		}
		if (!errorDir.isDirectory() && !errorDir.mkdir()) {
			logger.warn("Unable to create error directory, " + errorDir.getAbsolutePath());
		}
	}

	@SuppressWarnings("unchecked")
	public void setParserWtm(
			final List<WTM> wtms ) {
		((AbstractWeatherParser<WTM>) parser).setGrids(wtms);
	}

	@SuppressWarnings("unchecked")
	public void setParserRTNEPHQuarterGrid(
			final List<RTNEPHQuarterGrid> grids ) {
		((AbstractWeatherParser<RTNEPHQuarterGrid>) parser).setGrids(grids);
	}
}
