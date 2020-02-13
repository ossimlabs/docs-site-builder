package com.maxar.weather.dm.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Controller;

import com.maxar.manager.dataingest.Ingester;
import com.maxar.manager.manager.DataManager;
import com.maxar.weather.dm.configuration.WeatherProperties;
import com.maxar.weather.dm.dataingest.WeatherFileIngester;
import com.maxar.weather.parser.AbstractWeatherParser.MapGridType;
import com.maxar.weather.entity.map.RTNEPHQuarterGrid;
import com.maxar.weather.entity.map.WTM;
import com.maxar.weather.entity.weather.WeatherSet;
import com.maxar.weather.repository.RTNEPHQuarterGridRepository;
import com.maxar.weather.repository.WTMRepository;
import com.maxar.weather.repository.WeatherSetRepository;
import com.radiantblue.analytics.core.log.SourceLogger;

@Controller
public class WeatherDataManager extends
		DataManager
{
	private static Logger logger = SourceLogger.getLogger(WeatherDataManager.class.getName());

	private Duration dataOlderThan = null;

	@Value("${weatherdatamanager.ingesterSpringFile}")
	private String springFile = null;

	// need lists of ingesters, parsers, and databases
	private final Map<String, Ingester> ingesterMap = new HashMap<>();
	private final List<Ingester> ingesters = new ArrayList<>();

	@Autowired
	private WTMRepository wtmRepository;

	@Autowired
	private RTNEPHQuarterGridRepository rtnephRepository;

	@Autowired
	private WeatherSetRepository weatherSetRepository;

	@Override
	protected String getManagerName() {
		return "WeatherDataManager";
	}

	@Autowired
	private WeatherProperties weatherProperties;

	private void initializeParsers() {
		// load spring config file
		try (final GenericXmlApplicationContext appContext = new GenericXmlApplicationContext()) {

			appContext.load(springFile);

			final Properties props = new Properties();

			weatherProperties.getAFWAWeather()
					.forEach((
							key,
							value ) -> props.put(	"AFWAWeather." + key,
													value));

			weatherProperties.getRTNEPHWeather()
					.forEach((
							key,
							value ) -> props.put(	"RTNEPHWeather." + key,
													value));

			final PropertiesPropertySource ps = new PropertiesPropertySource(
					"config",
					props);
			appContext.getEnvironment()
					.getPropertySources()
					.addFirst(ps);
			appContext.refresh();

			final Map<String, WeatherFileIngester> fileIngesterMap = appContext
					.getBeansOfType(WeatherFileIngester.class);

			for (final Map.Entry<String, WeatherFileIngester> entry : fileIngesterMap.entrySet()) {
				ingesterMap.put(entry.getKey(),
								entry.getValue());
			}
		}

		for (final Ingester ing : ingesterMap.values()) {
			logger.info("Ingester: " + ing.getClass()
					.getName() + " found");
		}

		if (ingesterMap.isEmpty()) {
			logger.warn("No weather ingesters found in spring file");
		}
	}

	@Override
	protected void startIngest() {
		for (final Ingester ingester : ingesters) {
			startIngester(ingester);
		}
	}

	@Override
	protected void afterInit() {
		initializeParsers();

		final List<WTM> wtms = wtmRepository.findAllByOrderByIdAsc();
		final List<RTNEPHQuarterGrid> grids = rtnephRepository.findAllByOrderByNorthernHemisphereDescIdAsc();

		// Initialize weather ingesters
		for (final Ingester ingester : ingesterMap.values()) {
			// currently only file ingesters supported
			if (ingester instanceof WeatherFileIngester) {
				final WeatherFileIngester weatherFileIngester = (WeatherFileIngester) ingester;
				weatherFileIngester.setWeatherSetRepository(weatherSetRepository);
				if (weatherFileIngester.getParser()
						.getMapGridType() == MapGridType.WTM) {
					weatherFileIngester.setParserWtm(wtms);
				}
				else if (weatherFileIngester.getParser()
						.getMapGridType() == MapGridType.RTNEPHQuarterGrid) {
					weatherFileIngester.setParserRTNEPHQuarterGrid(grids);
				}
				else {
					logger.warn("Unrecognized map grid type for parser");
				}

				ingester.init();

				ingesters.add(ingester);

			}
			else {
				// currently only file ingesters supported
				logger.error("Ingester not a file ingester: " + ingester.getFeedId() + " : " + ingester.getFeedName());
			}
		}
	}

	private DateTime getNow() {
		return DateTime.now();
	}

	@Override
	protected void runCleanup() {
		logger.debug("Current time: " + getNow());

		if ((dataOlderThan != null) && (dataOlderThan.getStandardDays() > 0)) {
			final DateTime now = getNow();
			final DateTime maxDate = now.minus(dataOlderThan);

			logger.debug("Deleting weather data older than " + maxDate.toString("MM/dd/yyyy HH:mm:ss"));
			logger.debug("Using weatherset atTime < " + maxDate.getMillis());

			final List<WeatherSet> wxSets = weatherSetRepository
					.findByAtTimeMillisLessThanEqualOrderByAtTimeMillisDesc(maxDate.getMillis());
			for (final WeatherSet wxSet : wxSets) {
				weatherSetRepository.delete(wxSet);
			}
			logger.debug("Delete weather done");
		}
	}

	@PreDestroy
	public void cleanupTask() {
		logger.info("Running cleanup");
		super.cleanupManager();
		super.cleanupDataManager();
	}

	@Override
	protected void initManager() {
		// There is nothing to do here.
	}

	@EventListener
	public void launch(
			final ApplicationReadyEvent event ) {
		init();
		start();
	}

	@Value("${weatherdatamanager.dataOlderThanDays}")
	public void setDataOlderThan(
			final String dataOlderThanDays ) {
		dataOlderThan = Duration.standardDays(Integer.parseInt(dataOlderThanDays.trim()));
	}
}
