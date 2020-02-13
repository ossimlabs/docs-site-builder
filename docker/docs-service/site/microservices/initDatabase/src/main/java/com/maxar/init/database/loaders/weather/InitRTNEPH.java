package com.maxar.init.database.loaders.weather;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.maxar.init.database.utils.weather.RTNEPHGridGenerator;
import com.maxar.weather.entity.map.MapGrid;
import com.maxar.weather.repository.MapGridRepository;
import com.radiantblue.analytics.core.log.SourceLogger;

@EnableAutoConfiguration
@EnableJpaRepositories("com.maxar.weather.repository")
@EntityScan("com.maxar.weather.entity")
public class InitRTNEPH implements
		CommandLineRunner
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	@Autowired
	private MapGridRepository mapGridRepository;

	public void initRTNEPH() {
		BasicConfigurator.configure();

		final List<MapGrid> grids = RTNEPHGridGenerator.generateRTNEPHQuarterGrid(false);

		logger.info("RTNEPH Count = " + grids.size());

		mapGridRepository.saveAll(grids);
	}

	@Override
	public void run(
			final String... args )
			throws Exception {
		initRTNEPH();
	}

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				InitRTNEPH.class).properties("spring.config.name:initrtneph")
						.build()
						.run(args);
	}
}
