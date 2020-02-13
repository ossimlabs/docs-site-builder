package com.maxar.init.database.loaders.mission;

import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import com.maxar.asset.entity.Asset;
import com.maxar.init.database.utils.mission.InitMissionUtils;

@EnableAutoConfiguration
@ComponentScan(basePackages = {
	"com.maxar.init.database.utils.mission",
	"com.maxar.init.database.loaders.jpa.asset",
	"com.maxar.init.database.loaders.jpa.mission"
})
public class InitMissions implements
		CommandLineRunner
{
	@Autowired
	private InitMissionUtils initMissionUtils;

	@Value("${microservices.initdatabase.initmission.begintime}")
	private String beginTimeString;

	@Value("${microservices.initdatabase.initmission.endtime}")
	private String endTimeString;

	@Value("${microservices.initdatabase.initmission.nummissions:10}")
	private int numMissions;

	public void initMissions() {
		final DateTime beginTime = DateTime.parse(beginTimeString);
		final DateTime endTime = DateTime.parse(endTimeString);

		final List<String> airborneAssetIds = initMissionUtils.getAirborneAssets()
				.stream()
				.map(Asset::getId)
				.collect(Collectors.toList());

		for (int i = 0; i < numMissions; i++) {
			final int assetIndex = InitMissionUtils.randomGenerator.nextInt(airborneAssetIds.size());

			initMissionUtils.generateRandomMission(	i,
													beginTime,
													endTime,
													airborneAssetIds.get(assetIndex));
		}
	}

	@Override
	public void run(
			final String... args )
			throws Exception {
		initMissions();
	}

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				InitMissions.class).properties("spring.config.name:initmission")
						.build()
						.run(args);
	}
}
