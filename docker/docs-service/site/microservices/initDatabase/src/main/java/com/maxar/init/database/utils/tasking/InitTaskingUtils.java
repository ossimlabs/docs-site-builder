package com.maxar.init.database.utils.tasking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.maxar.asset.common.aircraft.AircraftTrackStateVectorProvider;
import com.maxar.mission.model.MissionModel;
import com.maxar.mission.repository.MissionRepository;
import com.maxar.opgen.types.TargetRequirement;
import com.maxar.planning.entity.link.Link;
import com.maxar.planning.entity.tasking.Tasking;
import com.maxar.planning.repository.TaskingRepository;
import com.maxar.target.entity.Target;
import com.radiantblue.analytics.core.akka.ClusterSystem;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.isr.core.component.accgen.IAccess;
import com.radiantblue.analytics.isr.core.component.accgen.IAccessRequest;
import com.radiantblue.analytics.isr.core.component.accgen.IAccessRequestProvider;
import com.radiantblue.analytics.isr.core.component.accgen.IBulkAccessGenerator;
import com.radiantblue.analytics.isr.core.component.accgen.IUntrimmedAccessData;
import com.radiantblue.analytics.isr.core.component.accgen.TrimmedAccessConstraintsFromAccessTarget;
import com.radiantblue.analytics.isr.core.component.schedule.IScheduler;
import com.radiantblue.analytics.isr.core.component.schedule.SinglePeriodScheduler;
import com.radiantblue.analytics.isr.core.component.schedule.WindowBasedScheduler;
import com.radiantblue.analytics.isr.core.component.schedule.score.IScoreFunction;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.isr.core.model.sensormode.ISensorMode;
import com.radiantblue.analytics.mechanics.IStateVectorProvider;

@Component
public class InitTaskingUtils
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	private ClusterSystem clusterSystem = null;
	private final IAccessRequestProvider accessProvider;
	private final IBulkAccessGenerator accessGenerator;
	private IScoreFunction scoreFunction;
	private final IScheduler scheduler;
	private static int cores = 8;
	private static long SHORT_ACCESS_MILLIS = 1000;
	public static Random randomGenerator = new Random(
			8675309);

	@Autowired
	MissionRepository missionRepository;

	@Autowired
	TaskingRepository taskingRepository;

	@Autowired
	MissionRetriever missionRetriever;

	public InitTaskingUtils() {

		try (GenericXmlApplicationContext appContext = new GenericXmlApplicationContext()) {

			appContext.load("ClusterConfig.xml");
			appContext.load("SchedulerConfig.xml");

			appContext.refresh();

			accessProvider = (IAccessRequestProvider) appContext.getBean("accessRequestProvider");
			accessGenerator = (IBulkAccessGenerator) appContext.getBean("accessGenerator");
			scheduler = (IScheduler) appContext.getBean("greedyAccessScheduler");
			scoreFunction = (IScoreFunction) appContext.getBean("scoreFunction");

			final WindowBasedScheduler wb = (WindowBasedScheduler) scheduler;
			((SinglePeriodScheduler) wb.getInternalScheduler()).setOpScoreFunction(scoreFunction);

			if (appContext.containsBean("clusterSystem")) {
				logger.info("Using cluster system with " + cores + " cores");
				clusterSystem = (ClusterSystem) appContext.getBean("clusterSystem");
			}

			clusterSystem.setNumWorkers(cores);
		}
	}

	public void terminate() {

		logger.info("Shutting down cluster system...");

		clusterSystem.shutDown();
	}

	public void generateTasking(
			final DateTime beginTime,
			final DateTime endTime,
			final List<Asset> airborneAssets,
			final List<TargetRequirement> targetRequirements,
			final Map<String, Link> linkMap,
			final Map<String, Target> targetMap ) {

		try {
			final List<MissionModel> allMissionModels = new ArrayList<>();
			final Map<Integer, Asset> assetIdToAssetMap = new HashMap<>();
			for (final Asset asset : airborneAssets) {
				final List<MissionModel> missionModels = missionRetriever.getMissions(	asset.getId(),
																						beginTime,
																						endTime);

				if (missionModels == null) {
					continue;
				}
				assetIdToAssetMap.put(	asset.getId(),
										asset);
				allMissionModels.addAll(missionModels);
			}

			for (final TargetRequirement targetRequirement : targetRequirements) {
				final Tasking missionTasking = generateMissionTasking(	assetIdToAssetMap,
																		allMissionModels,
																		beginTime,
																		endTime,
																		targetRequirement,
																		linkMap,
																		targetMap);

				if (missionTasking != null) {
					taskingRepository.saveAndFlush(missionTasking);
				}
			}
		}
		finally {
			terminate();
		}
	}

	private Tasking generateMissionTasking(
			final Map<Integer, Asset> assetIdToAssetMap,
			final List<MissionModel> missionModels,
			final DateTime beginTime,
			final DateTime endTime,
			final TargetRequirement targetRequirement,
			final Map<String, Link> linkMap,
			final Map<String, Target> targetMap ) {

		// randomly pick mission until a successful access is found
		final int listSize = missionModels.size();
		final List<MissionModel> copyMissionModels = new ArrayList<>(
				missionModels);
		for (int i = 0; i < listSize; i++) {
			final int randomIndex = randomGenerator.nextInt(copyMissionModels.size());

			final MissionModel missionModel = copyMissionModels.get(randomIndex);
			copyMissionModels.remove(randomIndex);

			final Asset asset = assetIdToAssetMap.get(Integer.parseInt(missionModel.getAssetId()));

			final IStateVectorProvider svp = new AircraftTrackStateVectorProvider(
					missionModel);

			asset.setPropagator(svp);
			asset.init();

			final List<IAccess> trimmedAccesses = generateAccesses(	beginTime,
																	endTime,
																	asset,
																	targetRequirement);

			if ((trimmedAccesses != null) && !trimmedAccesses.isEmpty()) {
				final Tasking tasking = convertAccessesToTasking(	trimmedAccesses,
																	missionModel,
																	linkMap,
																	targetMap);

				// only need one tasking per target, so done
				return tasking;
			}

		}

		return null;
	}

	private List<IAccess> generateAccesses(
			final DateTime beginTime,
			final DateTime endTime,
			final com.radiantblue.analytics.isr.core.model.asset.Asset asset,
			final TargetRequirement targetRequirement ) {
		final List<IAccessRequest> accessRequests = new ArrayList<>();

		accessRequests.addAll(accessProvider.accessRequests(beginTime,
															endTime,
															Collections.singletonList(asset),
															// Only one target per requirement
															Collections.singletonList(targetRequirement)));

		final List<IUntrimmedAccessData> rawUntrimmedAccesses = accessGenerator
				.generateUntrimmedAccesses(	accessRequests,
											new TrimmedAccessConstraintsFromAccessTarget());
		final List<IAccess> trimmedAccesses = new ArrayList<>();

		for (final IUntrimmedAccessData uad : rawUntrimmedAccesses) {

			final IAccess ua = uad.untrimmedAccess();

			if (!ua.isValid()) {
				continue;
			}

			for (final IAccess a : uad.validTrimmedAccesses()) {

				if (a.duration()
						.isShorterThan(Duration.millis(SHORT_ACCESS_MILLIS))) {

					logger.warn("Removing very short access (<" + SHORT_ACCESS_MILLIS + "ms): " + a.duration());
					continue;
				}

				if (a.source() instanceof ISensorMode) {
					trimmedAccesses.add(a);
				}
				else {
					logger.warn("Access source not ISensorMode: " + a.source()
							.getClass()
							.getName());
				}
			}
		}

		return trimmedAccesses;
	}

	private Tasking convertAccessesToTasking(
			final List<IAccess> trimmedAccesses,
			final MissionModel missionModel,
			final Map<String, Link> linkMap,
			final Map<String, Target> targetMap ) {
		Tasking tasking = null;
		for (final IAccess trimmedAccess : trimmedAccesses) {
			if (!(trimmedAccess.dest() instanceof TargetRequirement)) {
				logger.warn("Access dest not instance of TargetRequirement?");
				continue;
			}
			if (!(trimmedAccess.source() instanceof ISensorMode)) {
				logger.warn("Access source not instance of ISensorMode?");
				continue;
			}
			final ISensorMode sensorMode = (ISensorMode) trimmedAccess.source();
			final TargetRequirement targetRequirement = (TargetRequirement) trimmedAccess.dest();
			final Target target = targetMap.get(targetRequirement.getName());
			final Link link = linkMap.get(targetRequirement.getName());
			final Geometry targetCentroid = target.getGeometry()
					.getCentroid();
			tasking = new Tasking(
					missionModel.getId(),
					targetCentroid,
					targetCentroid,
					sensorMode.sensor()
							.getName(),
					sensorMode.sensor()
							.getSensorType(),
					sensorMode.getName(),
					0, // sensorPed
					0, // sensorAdhoc
					0, // sensor slots
					trimmedAccess.startTime()
							.getMillis(),
					trimmedAccess.endTime()
							.getMillis(),
					0, // num images
					100, // priority
					trimmedAccess.startTime()
									.getMillis(),
					"TASKING AUTHORITY",
					"STANDING",
					"SCENE",
					false, // complex
					link);

		}

		return tasking;
	}
}
