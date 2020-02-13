package com.maxar.init.database.loaders.tasking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import com.maxar.asset.entity.Asset;
import com.maxar.common.utils.GeoUtils;
import com.maxar.init.database.utils.planning.InitAssetUtils;
import com.maxar.init.database.utils.planning.InitPlanningUtils;
import com.maxar.init.database.utils.planning.InitTargetUtils;
import com.maxar.init.database.utils.tasking.InitTaskingUtils;
import com.maxar.opgen.types.TargetRequirement;
import com.maxar.planning.entity.link.Link;
import com.maxar.target.entity.Target;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;

@EnableAutoConfiguration
@ComponentScan(basePackages = {
	"com.maxar.init.database.utils.tasking",
	"com.maxar.init.database.utils.planning",
	"com.maxar.init.database.loaders.jpa.planning",
	"com.maxar.init.database.loaders.jpa.target",
	"com.maxar.init.database.loaders.jpa.asset",
	"com.maxar.init.database.loaders.jpa.tasking"
})
public class InitTasking implements
		CommandLineRunner
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	@Autowired
	private InitTaskingUtils initTaskingUtils;

	@Autowired
	private InitAssetUtils initAssetUtils;

	@Autowired
	private InitTargetUtils initTargetUtils;

	@Autowired
	private InitPlanningUtils initPlanningUtils;

	@Value("${microservices.initdatabase.inittasking.begintime}")
	private String beginTimeString;

	@Value("${microservices.initdatabase.inittasking.endtime}")
	private String endTimeString;

	public void initTasking() {
		final DateTime beginTime = DateTime.parse(beginTimeString);
		final DateTime endTime = DateTime.parse(endTimeString);

		// Get all airborne assets
		final List<Asset> rawAirborneAssets = initAssetUtils.getAirborneAssets();
		final List<com.radiantblue.analytics.isr.core.model.asset.Asset> airborneAssets = initAssetUtils
				.buildAirborneAssets(rawAirborneAssets);

		// Get all targets
		final List<Target> targets = initTargetUtils.getInitTargets();

		logger.debug("Target count:" + targets.size() + " " + DateTime.now());

		// create rb-analytics Irequirement and PlanningRepository Link map for each
		// target
		final List<TargetRequirement> targetRequirements = new ArrayList<>();

		final Map<String, Link> linkMap = new HashMap<>();
		final Map<String, Target> targetMap = new HashMap<>();
		for (final Target target : targets) {
			final Geometry geom = GeoUtils.convertDegreesToRadians(target.getGeometry());
			targetRequirements.add(new TargetRequirement(
					target.getTargetId(),
					GeodeticGeometry.create(geom)));
			linkMap.put(target.getTargetId(),
						initPlanningUtils.generateLink(target.getTargetId()));
			targetMap.put(	target.getTargetId(),
							target);
		}

		// Generate schedule for "requirements" against airborneAssets
		initTaskingUtils.generateTasking(	beginTime,
											endTime,
											airborneAssets,
											targetRequirements,
											linkMap,
											targetMap);
	}

	@Override
	public void run(
			final String... args )
			throws Exception {
		initTasking();
	}

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				InitTasking.class).properties("spring.config.name:inittasking")
						.build()
						.run(args);
	}
}
