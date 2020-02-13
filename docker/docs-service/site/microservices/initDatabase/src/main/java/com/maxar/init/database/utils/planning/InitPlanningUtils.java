package com.maxar.init.database.utils.planning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.maxar.opgen.types.TargetRequirement;
import com.maxar.planning.entity.image.CollectionWindow;
import com.maxar.planning.entity.image.ImageFrame;
import com.maxar.planning.entity.image.ImageOp;
import com.maxar.planning.entity.link.Link;
import com.maxar.planning.repository.CollectionWindowRepository;
import com.maxar.planning.repository.LinkRepository;
import com.maxar.target.entity.Target;
import com.radiantblue.analytics.aerospace.geometry.PointToPointGeometry;
import com.radiantblue.analytics.core.Context;
import com.radiantblue.analytics.core.akka.ClusterSystem;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.isr.core.component.accgen.IAccess;
import com.radiantblue.analytics.isr.core.component.accgen.IAccessRequest;
import com.radiantblue.analytics.isr.core.component.accgen.IAccessRequestProvider;
import com.radiantblue.analytics.isr.core.component.accgen.IBulkAccessGenerator;
import com.radiantblue.analytics.isr.core.component.accgen.IUntrimmedAccessData;
import com.radiantblue.analytics.isr.core.component.accgen.TrimmedAccessConstraintsFromAccessTarget;
import com.radiantblue.analytics.isr.core.component.schedule.ConstellationSchedule;
import com.radiantblue.analytics.isr.core.component.schedule.ISchedule;
import com.radiantblue.analytics.isr.core.component.schedule.IScheduler;
import com.radiantblue.analytics.isr.core.component.schedule.ISchedulerEventsCallback;
import com.radiantblue.analytics.isr.core.component.schedule.InvalidAssetSchedule;
import com.radiantblue.analytics.isr.core.component.schedule.SchedulerContext;
import com.radiantblue.analytics.isr.core.component.schedule.SchedulerContext.RichSchedulerContext;
import com.radiantblue.analytics.isr.core.component.schedule.SinglePeriodScheduler;
import com.radiantblue.analytics.isr.core.component.schedule.WindowBasedScheduler;
import com.radiantblue.analytics.isr.core.component.schedule.score.IScoreFunction;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.isr.core.model.asset.IAsset;
import com.radiantblue.analytics.isr.core.model.sensormode.ISensorMode;
import com.radiantblue.analytics.isr.core.op.EOOp;
import com.radiantblue.analytics.isr.core.op.IBeam;
import com.radiantblue.analytics.isr.core.op.IEOBeam;
import com.radiantblue.analytics.isr.core.op.IModeOp;
import com.radiantblue.analytics.isr.core.op.IScheduleOp;
import com.radiantblue.analytics.isr.core.op.InvalidModeOp;
import com.radiantblue.analytics.isr.core.op.SAROp;
import com.radiantblue.analytics.isr.sar.ISARBeam;

import scala.collection.JavaConverters;

@Component
public class InitPlanningUtils
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	ClusterSystem clusterSystem = null;
	private final IAccessRequestProvider accessProvider;
	private final IBulkAccessGenerator accessGenerator;
	private IScoreFunction scoreFunction;
	private final IScheduler scheduler;
	private static int cores = 8;
	private static long SHORT_ACCESS_MILLIS = 1000;

	private static int cwCounter = 0;
	private static int opCounter = 0;
	private static int frameCounter = 0;

	@Value("${microservices.initdatabase.initplanning.cwBufferMS:10000}")
	private int cwBufferMS;

	@Value("#{T(org.joda.time.Duration).standardMinutes(${microservices.initdatabase.initplanning.cwMaxDurationMinutes:60})}")
	private Duration cwMaxDuration;

	@Autowired
	private LinkRepository linkRepository;

	@Autowired
	private CollectionWindowRepository collectionWindowRepository;

	public InitPlanningUtils() {

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

	public void generateSchedule(
			final DateTime beginTime,
			final DateTime endTime,
			final List<com.radiantblue.analytics.isr.core.model.asset.Asset> spaceAssets,
			final Map<String, String> assetNameToScnMap,
			final List<TargetRequirement> targetRequirements,
			final Map<String, Link> linkMap,
			final Map<String, Target> targetMap ) {

		try {
			final List<IAccess> trimmedAccesses = generateAccesses(	beginTime,
																	endTime,
																	spaceAssets,
																	targetRequirements);
			logger.debug("Number of trimmed accesses:" + trimmedAccesses.size() + " " + DateTime.now());

			final List<CollectionWindow> cws = generatePlanningData(beginTime,
																	endTime,
																	trimmedAccesses,
																	spaceAssets,
																	assetNameToScnMap,
																	linkMap,
																	targetMap);

			logger.debug("Generated Cws:" + cws.size() + " at " + DateTime.now());

			collectionWindowRepository.saveAll(cws);
		}
		finally {
			terminate();
		}
	}

	private List<CollectionWindow> generatePlanningData(
			final DateTime beginTime,
			final DateTime endTime,
			final List<IAccess> trimmedAccesses,
			final List<com.radiantblue.analytics.isr.core.model.asset.Asset> spaceAssets,
			final Map<String, String> assetNameToScnMap,
			final Map<String, Link> linkMap,
			final Map<String, Target> targetMap ) {

		final ConstellationSchedule initialSchedule = ConstellationSchedule.createEmpty(beginTime,
																						endTime,
																						Lists.newArrayList(spaceAssets));

		Context scheduleContext = SchedulerContext.withBasics(	beginTime,
																endTime,
																trimmedAccesses,
																initialSchedule);

		// Insert logging as needed
		scheduleContext = new RichSchedulerContext(
				scheduleContext).withScheduleEventsCallback(new ISchedulerEventsCallback() {

					@Override
					public void onValidOps(
							final List<IModeOp> validOps ) {}

					@Override
					public void onOpScheduled(
							final IModeOp op ) {}

					@Override
					public void onOpFailedToSchedule(
							final ISchedule originalSched,
							final IModeOp op,
							final InvalidAssetSchedule invalidSched ) {}

					@Override
					public void onInvalidOps(
							final List<InvalidModeOp> invalidOps ) {}

					@Override
					public void onAddingOp(
							final IModeOp op ) {}
				});

		final ISchedule schedule = scheduler.makeSchedule(scheduleContext);

		return convertIScheduleToCws(	schedule,
										linkMap,
										targetMap,
										spaceAssets,
										assetNameToScnMap);
	}

	private List<CollectionWindow> convertIScheduleToCws(
			final ISchedule schedule,
			final Map<String, Link> linkMap,
			final Map<String, Target> targetMap,
			final List<com.radiantblue.analytics.isr.core.model.asset.Asset> spaceAssets,
			final Map<String, String> assetNameToScnMap ) {
		// create an asset map
		final Map<String, com.radiantblue.analytics.isr.core.model.asset.Asset> assetMap = spaceAssets.stream()
				.collect(Collectors.toMap(	asset -> asset.getName(),
											asset -> asset));

		// create a map of asset name to ImageOp for cw calculation
		final Map<String, List<ImageOp>> assetNameToImageOpMap = new HashMap<>();

		final Collection<IScheduleOp> scalaOps = JavaConverters.asJavaCollectionConverter(schedule.allOps())
				.asJavaCollection();

		final List<IScheduleOp> scheduleOps = Lists.newArrayList(scalaOps);

		// only interested in mode ops
		for (final IScheduleOp scheduleOp : scheduleOps) {

			if (scheduleOp.isValid()) {

				if (scheduleOp instanceof IModeOp) {
					final IModeOp modeOp = (IModeOp) scheduleOp;

					// this should be target id
					final String name = modeOp.target()
							.getName();
					final Link link = linkMap.get(name);
					final Target target = targetMap.get(name);

					final ImageOp imageOp = convertModeOpToImageOp(	modeOp,
																	link,
																	target);

					final String assetName = modeOp.asset()
							.getName();
					List<ImageOp> imageOps = null;
					if (assetNameToImageOpMap.get(assetName) == null) {
						imageOps = new ArrayList<>();
						assetNameToImageOpMap.put(	assetName,
													imageOps);
					}
					assetNameToImageOpMap.get(assetName)
							.add(imageOp);
				}
				else {
					logger.debug("OTHER OP: " + scheduleOp.getClass()
							.getCanonicalName());
				}
			}
		}

		return createCws(	assetNameToImageOpMap,
							assetMap,
							assetNameToScnMap);
	}

	private List<CollectionWindow> createCws(
			final Map<String, List<ImageOp>> assetNameToImageOpMap,
			final Map<String, Asset> assetMap,
			final Map<String, String> assetNameToScnMap ) {

		final List<CollectionWindow> cws = new ArrayList<>();
		for (final String assetName : assetNameToImageOpMap.keySet()) {
			final List<ImageOp> imageOps = assetNameToImageOpMap.get(assetName);
			Collections.sort(imageOps);

			// ops in start order now. Use maxCwDuration to break into cws
			final DateTime cwStart = new DateTime(
					imageOps.get(0)
							.getOpStartTimeMillis());
			final DateTime cwMaxEnd = cwStart.plus(cwMaxDuration);
			Set<ImageOp> cwImageOps = new HashSet<>();
			DateTime currentCwEnd = new DateTime(
					imageOps.get(0)
							.getOpEndTimeMillis());

			for (int i = 0; i < imageOps.size(); i++) {
				if (!cwMaxEnd.isBefore(imageOps.get(i)
						.getOpEndTimeMillis())) {
					cwImageOps.add(imageOps.get(i));
					currentCwEnd = new DateTime(
							imageOps.get(i)
									.getOpEndTimeMillis());
				}
				else {
					if (!cwImageOps.isEmpty()) {
						final String cwId = "CW: " + cwCounter++;
						final long cwStartMillis = cwStart.getMillis() - cwBufferMS;
						final long cwEndMillis = currentCwEnd.getMillis() + cwBufferMS;
						final int scn = Integer.parseInt(assetNameToScnMap.get(assetName));
						final String status = "SCHEDULED";

						final CollectionWindow cw = new CollectionWindow(
								cwId,
								status,
								assetName,
								scn,
								cwStartMillis,
								cwEndMillis);
						cw.setImageOps(cwImageOps);

						cwImageOps.stream()
								.forEach(x -> x.setCollectionWindow(cw));

						cws.add(cw);
						cwImageOps = new HashSet<>();
					}
				}
			}
		}
		return cws;
	}

	private ImageOp convertModeOpToImageOp(
			final IModeOp modeOp,
			final Link link,
			final Target target ) {
		final ImageOp imageOp = new ImageOp();

		imageOp.setOpId("IMAGEOP_" + opCounter++);
		imageOp.setOpStartTimeMillis(modeOp.startTime()
				.getMillis());
		imageOp.setOpEndTimeMillis(modeOp.endTime()
				.getMillis());
		imageOp.setTargetType(target.getTargetType()
				.name());
		imageOp.setCountryCode(target.getCountryCode());
		imageOp.setSensorType(modeOp.sensor()
				.getSensorType());
		imageOp.setSensorMode(modeOp.mode()
				.getName());
		final PointToPointGeometry ptp = modeOp.getGeometryAtTime(modeOp.startTime());
		// pull out quality and/or GSD
		double quality = 0.0;
		Length gsd = Length.Zero();

		if (modeOp instanceof EOOp) {
			quality = ((EOOp) modeOp).niirs();
			gsd = ((EOOp) modeOp).gsd();
		}
		else if (modeOp instanceof SAROp) {
			quality = ((SAROp) modeOp).rniirs();
		}
		else {
			logger.warn("Unexpected ModeOp, quality cannot be determined: " + modeOp.getClass()
					.getName());
		}

		imageOp.setElevation((int) Angle.fromDegrees(90.0)
				.minus(ptp.grazingAngle_atDest())
				.degrees());
		imageOp.setAzimuth(ptp.azOffNorth_atDest()
				.degrees());
		imageOp.setNiirs(quality);
		imageOp.setGsdOrIprMeters((gsd == null) ? 0.0 : gsd.meters());
		imageOp.setLink(link);
		final Set<ImageFrame> frames = new HashSet<>();
		for (final IBeam beam : modeOp.beams()) {
			if (beam.polygon() != null) {
				final ImageFrame frame = new ImageFrame();
				frame.setImageId("IMAGEID_" + frameCounter++);
				frame.setStartTimeMillis(beam.getStartTime()
						.getMillis());
				frame.setStopTimeMillis(beam.getEndTime()
						.getMillis());
				double beamQuality = 0.0;
				Length beamGsd = Length.Zero();

				if (beam instanceof IEOBeam) {
					beamQuality = ((IEOBeam) beam).getNiirs();
					beamGsd = ((IEOBeam) beam).getGsd();
				}
				else if (beam instanceof ISARBeam) {
					beamQuality = ((ISARBeam) beam).getRniirs();
				}
				else {
					logger.warn("Unexpected IBeam class, quality cannot be determined: " + beam.getClass()
							.getName());
				}
				frame.setNiirs(beamQuality);
				frame.setGsdMeters(beamGsd.meters());
				frame.setPolygon(beam.polygon()
						.jtsGeometry_deg());
				frame.setCenterCoord(frame.getPolygon()
						.getCentroid());
				frame.setImageOp(imageOp);
				frames.add(frame);
			}
		}
		imageOp.setImageFrames(frames);

		return imageOp;
	}

	private List<IAccess> generateAccesses(
			final DateTime beginTime,
			final DateTime endTime,
			final List<com.radiantblue.analytics.isr.core.model.asset.Asset> spaceAssets,
			final List<TargetRequirement> targetRequirements ) {
		final List<IAccessRequest> accessRequests = new ArrayList<>();
		for (final TargetRequirement targetRequirement : targetRequirements) {

			final List<IAsset> assetsToUse = new ArrayList<IAsset>(
					spaceAssets);

			accessRequests.addAll(accessProvider.accessRequests(beginTime,
																endTime,
																assetsToUse,
																// Only one target per requirement
																Collections.singletonList(targetRequirement)));
		}

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

		logger.info("Done");

		return trimmedAccesses;
	}

	public Link generateLink(
			final String targetId ) {
		final String crId = "CR:" + targetId;

		// ensure unique...crid should be unique
		final List<Link> dbLinks = linkRepository.findByCrId(crId);
		Link dbLink = null;
		if (dbLinks != null) {
			// should only be one , but search anyway
			for (final Link link : dbLinks) {
				if (link.getTargetId()
						.equals(targetId)) {
					dbLink = link;
					break;
				}
			}
		}

		if (dbLink == null) {
			final Link link = new Link(
					crId,
					targetId);
			dbLink = linkRepository.save(link);
		}

		return dbLink;
	}
}
