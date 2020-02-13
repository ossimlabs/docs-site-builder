package com.maxar.init.database.loaders.weather;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.maxar.init.database.utils.weather.WacAtcWtmLoader;
import com.maxar.weather.entity.map.ATC;
import com.maxar.weather.entity.map.MapGrid;
import com.maxar.weather.entity.map.WAC;
import com.maxar.weather.entity.map.WTM;
import com.maxar.weather.repository.MapGridRepository;
import com.radiantblue.analytics.core.log.SourceLogger;

@EnableAutoConfiguration
@EnableJpaRepositories("com.maxar.weather.repository")
@EntityScan("com.maxar.weather.entity")
public class InitWTM implements
		CommandLineRunner
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	@Autowired
	private MapGridRepository mapGridRepository;

	public void initWTM(
			final String wacFile ) {

		BasicConfigurator.configure();

		final boolean create = true;

		if (create) {
			final List<WAC> wacs = WacAtcWtmLoader.createAFWAWacs(wacFile);

			final List<MapGrid> grids = new ArrayList<MapGrid>();

			for (final WAC wac : wacs) {
				grids.add(wac);
				for (final ATC atc : wac.getAtcs()) {
					grids.add(atc);
					for (final WTM wtm : atc.getWtms()) {
						grids.add(wtm);
					}
				}
			}

			logger.info("grids: " + grids.size());
			mapGridRepository.saveAll(grids);

			logger.info("Done");

		}
	}

	@Override
	public void run(
			final String... args )
			throws Exception {
		final String wacFile = "wacgrid.txt";
		final URL url = Thread.currentThread()
				.getContextClassLoader()
				.getResource(wacFile);
		if (url == null) {
			throw new RuntimeException(
					"Cannot find resource on classpath: '" + wacFile + "'");
		}
		final String file = url.getFile();
		initWTM(file);
	}

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				InitWTM.class).properties("spring.config.name:initwtm")
						.build()
						.run(args);
	}
}
