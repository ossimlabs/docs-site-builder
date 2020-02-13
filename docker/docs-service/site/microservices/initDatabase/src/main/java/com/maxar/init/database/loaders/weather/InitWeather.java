package com.maxar.init.database.loaders.weather;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.maxar.weather.entity.map.RTNEPHQuarterGrid;
import com.maxar.weather.entity.map.WTM;
import com.maxar.weather.entity.weather.WeatherSet;
import com.maxar.weather.parser.AfwaWeatherParser;
import com.maxar.weather.parser.RTNEPHQuarterGridWeatherParser;
import com.maxar.weather.repository.RTNEPHQuarterGridRepository;
import com.maxar.weather.repository.WTMRepository;
import com.maxar.weather.repository.WeatherSetRepository;
import com.radiantblue.analytics.core.log.SourceLogger;

@EnableAutoConfiguration
@EnableJpaRepositories("com.maxar.weather.repository")
@EntityScan("com.maxar.weather.entity")
public class InitWeather implements
		CommandLineRunner
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	@Autowired
	private WTMRepository wtmRepository;

	@Autowired
	private RTNEPHQuarterGridRepository rtnephRepository;

	@Autowired
	private WeatherSetRepository weatherSetRepository;

	private String wtmWeatherFile;
	private String rtnephWeatherFile;

	static AfwaWeatherParser afwaParser = new AfwaWeatherParser();
	static RTNEPHQuarterGridWeatherParser rtnephParser = new RTNEPHQuarterGridWeatherParser();
	static List<WTM> wtms;
	static List<RTNEPHQuarterGrid> grids;

	static private String ApplicationName = "initweather";
	static private String PropertiesFile = ApplicationName + ".properties";

	private void loadProperties() {
		final Properties props = new Properties();

		final InputStream is = Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(PropertiesFile);

		if (is == null) {
			logger.error("Unable to load Generic Manager properties: " + PropertiesFile);
			System.exit(0);
		}

		logger.info("Loading properties: " + PropertiesFile);
		try {
			props.load(is);
		}
		catch (final IOException e) {

			logger.error(	"Failed to load : " + PropertiesFile + "\n",
							e);
		}

		String p;
		if ((p = props.getProperty("initDatabase.wtmWeatherFile")) != null) {
			wtmWeatherFile = p.trim();
		}
		if ((p = props.getProperty("initDatabase.rtnephWeatherFile")) != null) {
			rtnephWeatherFile = p.trim();
		}
	}

	public void initWeather(
			final String afwaFile,
			final String rtnephFile ) {

		BasicConfigurator.configure();
		DateTimeZone.setDefault(DateTimeZone.UTC);

		try (final InputStream afwaInput = openInputStream(afwaFile);
				final InputStream rtnephInput = openInputStream(rtnephFile)) {

			if (afwaInput != null) {
				wtms = wtmRepository.findAll(new Sort(
						Sort.Direction.ASC,
						"id"));

				afwaParser.setGrids(wtms);

				final List<WeatherSet> wx = afwaParser.parseData(	afwaInput,
																	new File(
																			afwaFile).getName());

				// create or update db
				for (final WeatherSet ws : wx) {
					final long records = weatherSetRepository.deleteByAtTimeMillis(ws.getAtTimeMillis());

					logger.debug("Deleted " + records + " existing weather sets");

					weatherSetRepository.saveAndFlush(ws);
				}

			}

			// Load all RTNEPH Quarter grids from db in hemisphere, then id
			// order
			if (rtnephInput != null) {
				grids = rtnephRepository.findAll(new Sort(
						Sort.Direction.ASC,
						"id"));

				rtnephParser.setGrids(grids);

				final List<WeatherSet> wx = rtnephParser.parseData(	rtnephInput,
																	null);

				// create or update db
				for (final WeatherSet ws : wx) {
					final long records = weatherSetRepository.deleteByAtTimeMillis(ws.getAtTimeMillis());

					logger.debug("Deleted " + records + " existing weather sets");

					weatherSetRepository.saveAndFlush(ws);
				}
			}

		}
		catch (final Exception ex) {
			logger.error("Exception trying to ingest weather:" + ex);
		}
	}

	private static InputStream openInputStream(
			final String fileName ) {

		if (fileName == null) {
			return null;
		}

		InputStream input = null;
		try {
			final File f = new File(
					fileName);

			if (f.getName()
					.toLowerCase()
					.endsWith("gz")) {
				input = new GZIPInputStream(
						new FileInputStream(
								f));
			}
			else {
				input = new FileInputStream(
						f);
			}
		}
		catch (final Exception ex) {
			logger.error("Exception trying to open file: " + fileName);
		}

		return input;
	}

	@Override
	public void run(
			final String... args )
			throws Exception {
		loadProperties();

		initWeather(wtmWeatherFile,
					rtnephWeatherFile);
	}

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				InitWeather.class).properties("spring.config.name:initweather")
						.build()
						.run(args);
	}
}
