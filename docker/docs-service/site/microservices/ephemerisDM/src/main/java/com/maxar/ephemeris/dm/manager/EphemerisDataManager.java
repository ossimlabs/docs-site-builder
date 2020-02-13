package com.maxar.ephemeris.dm.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Controller;

import com.maxar.ephemeris.dm.configuration.EphemerisProperties;
import com.maxar.ephemeris.dm.dataingest.EphemerisFileIngester;
import com.maxar.ephemeris.repository.EphemerisRepository;
import com.maxar.manager.dataingest.Ingester;
import com.maxar.manager.manager.DataManager;
import com.radiantblue.analytics.core.log.SourceLogger;

@Controller
public class EphemerisDataManager extends
		DataManager
{
	private static Logger logger = SourceLogger.getLogger(EphemerisDataManager.class.getName());

	private List<EphemerisFileIngester> ingesters;

	@Value("${ephemerisdatamanager.ingesterSpringFile}")
	private String springFile = null;

	@Autowired
	private EphemerisProperties ephemerisProperties;

	@Autowired
	private EphemerisRepository repository;

	@Override
	protected void startIngest() {
		for (final Ingester ingester : ingesters) {
			startIngester(ingester);
		}
	}

	@Override
	protected void initManager() {
		// there is nothing to do here
	}

	protected void initializeParsers() {
		try (final GenericXmlApplicationContext appContext = new GenericXmlApplicationContext()) {
			appContext.load(springFile);

			final Properties props = new Properties();

			ephemerisProperties.getIngesterProperties()
					.forEach(props::put);

			final PropertiesPropertySource ps = new PropertiesPropertySource(
					"config",
					props);
			appContext.getEnvironment()
					.getPropertySources()
					.addFirst(ps);
			appContext.refresh();

			final Map<String, EphemerisFileIngester> ingesterMap = appContext
					.getBeansOfType(EphemerisFileIngester.class);

			ingesters = new ArrayList<>(
					ingesterMap.values());
		}
	}

	@Override
	protected void afterInit() {
		initializeParsers();

		for (final EphemerisFileIngester ingester : ingesters) {
			ingester.init();
			ingester.setRepository(repository);
		}
	}

	@Override
	protected String getManagerName() {
		return "EphemerisDataManager";
	}

	@EventListener
	public void launch(
			final ApplicationReadyEvent event ) {
		init();
		start();
	}

	@PreDestroy
	public void cleanupTask() {
		logger.info("Running cleanup");
		super.cleanupManager();
		super.cleanupDataManager();
	}
}
