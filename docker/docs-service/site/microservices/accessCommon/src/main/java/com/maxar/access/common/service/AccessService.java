package com.maxar.access.common.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.maxar.access.common.types.Requirement;
import com.maxar.access.common.types.WeatherConstraint;
import com.maxar.access.common.utils.AccessTrimmingConstraintProvider;
import com.maxar.access.common.utils.ConstraintUtils;
import com.maxar.access.model.Access;
import com.maxar.access.model.AccessValues;
import com.maxar.access.model.AdhocMission;
import com.maxar.access.model.UntrimmedAccess;
import com.maxar.asset.common.aircraft.AircraftTrackStateVectorProvider;
import com.maxar.asset.common.exception.MissionIdDoesNotExistException;
import com.maxar.asset.common.exception.NoEphemerisFoundException;
import com.maxar.asset.common.exception.NoMissionsFoundException;
import com.maxar.asset.common.exception.PropagatorTypeDoesNotExistException;
import com.maxar.asset.common.service.ApiService;
import com.maxar.common.types.Vector3D;
import com.maxar.common.utils.GeoUtils;
import com.maxar.target.model.OrderOfBattle;
import com.maxar.target.model.TargetType;
import com.maxar.terrain.model.TimeInterval;
import com.radiantblue.analytics.aerospace.geometry.PointToPointGeometry;
import com.radiantblue.analytics.aerospace.geometry.constraint.FailedGeometryConstraintException;
import com.radiantblue.analytics.core.constraint.ConstraintException;
import com.radiantblue.analytics.core.constraint.IAccessConstraint;
import com.radiantblue.analytics.core.constraint.IConstraintException;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;
import com.radiantblue.analytics.isr.core.component.accgen.IAccess;
import com.radiantblue.analytics.isr.core.component.accgen.IAccessRequest;
import com.radiantblue.analytics.isr.core.component.accgen.IAccessRequestProvider;
import com.radiantblue.analytics.isr.core.component.accgen.IBulkAccessGenerator;
import com.radiantblue.analytics.isr.core.component.accgen.IUntrimmedAccessData;
import com.radiantblue.analytics.isr.core.component.accgen.constraint.SensorTypeConstraint;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.isr.core.model.asset.IAsset;
import com.radiantblue.analytics.isr.core.model.asset.Satellite;
import com.radiantblue.analytics.isr.core.model.requirement.constraint.QualityConstraint;
import com.radiantblue.analytics.isr.core.model.sensormode.ISensorMode;
import com.radiantblue.analytics.isr.eo.IEOSensor;
import com.radiantblue.analytics.mechanics.IStateVectorProvider;
import com.radiantblue.analytics.mechanics.orbit.revandpass.IRevAndPass;
import com.radiantblue.analytics.mechanics.orbit.revandpass.IRevAndPassCalculator;

/**
 * Responsible for doing the work for the access service's endpoints.
 */
@Component
public class AccessService
{

	private static Logger logger = SourceLogger.getLogger(AccessService.class.getName());

	@Autowired
	private SupportingServiceClient serviceClient;

	@Autowired
	private ApiService assetClient;

	private IBulkAccessGenerator accessGenerator;
	private IAccessRequestProvider accessProvider;

	private static final String schedulerConfigFile = "SchedulerConfig.xml";
	private static final String clusterConfigFile = "ClusterConfig.xml";

	@PostConstruct
	public void init() {
		logger.info("Initializing access generation context.");

		try (final GenericXmlApplicationContext appContext = new GenericXmlApplicationContext()) {
			appContext.load(schedulerConfigFile);
			appContext.load(clusterConfigFile);
			appContext.refresh();

			accessGenerator = (IBulkAccessGenerator) appContext.getBean("accessGenerator");

			accessProvider = (IAccessRequestProvider) appContext.getBean("accessRequestProvider");
		}
	}

	public List<UntrimmedAccess> getAccesses(
			final List<Asset> assets,
			final DateTime start,
			final DateTime end,
			final List<IAccessConstraint> constraints,
			final String sensorType,
			final Geometry geometry,
			final TargetType targetType,
			final String propagatorType,
			final OrderOfBattle orderOfBattle ) {

		final Map<String, List<IAccessConstraint>> assetConstraints = new HashMap<>();
		final Map<String, String> assetPropagatorTypes = new HashMap<>();

		for (final Asset asset : assets) {

			String assetPropagatorType = null;
			try {
				assetPropagatorType = assetClient.updateAssetEphemeris(	asset,
																		start,
																		propagatorType);
			}
			catch (PropagatorTypeDoesNotExistException | NoEphemerisFoundException e) {
				logger.error(e.getMessage());
			}

			assetPropagatorTypes.put(	asset.getName(),
										assetPropagatorType);

			assetConstraints.put(	asset.getName(),
									asset.accessConstraints());
		}

		return generateAccesses(geometry,
								constraints,
								sensorType,
								targetType,
								orderOfBattle,
								start,
								end,
								assets,
								assetConstraints);
	}

	public List<UntrimmedAccess> getAirborneAccesses(
			final List<Asset> assets,
			final AdhocMission adhocMission,
			final DateTime start,
			final DateTime end,
			final List<IAccessConstraint> constraints,
			final String sensorType,
			final Geometry geometry,
			final TargetType targetType,
			final OrderOfBattle orderOfBattle ) {

		final Map<String, List<IAccessConstraint>> assetConstraints = new HashMap<>();

		if (adhocMission == null) {
			final Iterator<Asset> assetIter = assets.iterator();
			while (assetIter.hasNext()) {
				final Asset asset = assetIter.next();

				try {
					assetClient.updateAssetMission(	asset,
													start,
													end,
													null);
				}
				catch (NoMissionsFoundException | MissionIdDoesNotExistException e) {
					logger.error(e.getMessage());
					assetIter.remove();
					continue;
				}

				assetConstraints.put(	asset.getName(),
										asset.accessConstraints());
			}

			if (assets.isEmpty()) {
				return Collections.emptyList();
			}
		}
		else {
			for (final Asset asset : assets) {
				generateAssetMission(	asset,
										adhocMission);
			}
		}

		return generateAccesses(geometry,
								constraints,
								sensorType,
								targetType,
								orderOfBattle,
								start,
								end,
								assets,
								assetConstraints);
	}

	private List<UntrimmedAccess> generateAccesses(
			final Geometry geometry,
			final List<IAccessConstraint> constraints,
			final String sensorType,
			final TargetType targetType,
			final OrderOfBattle orderOfBattle,
			final DateTime start,
			final DateTime end,
			final List<Asset> assets,
			final Map<String, List<IAccessConstraint>> assetConstraints ) {

		final Geometry geometryInRadians = GeoUtils.convertDegreesToRadians(geometry);
		final Requirement requirement = buildRequirement(	constraints,
															sensorType,
															geometryInRadians,
															targetType,
															orderOfBattle);

		final List<IAccessRequest> accessRequests = accessProvider.accessRequests(	start,
																					end,
																					assets,
																					Collections
																							.singletonList(requirement),
																					requirement);

		// Terrain and weather constraints don't cooperate with the
		// AccessTrimmingConstraintProvider and are applied manually below
		final List<IAccessConstraint> constraintsForTrimming = constraints.stream()
				.filter(c -> !c.getName()
						.equals(ConstraintUtils.terrainConstraintName))
				.filter(c -> !c.getName()
						.equals(ConstraintUtils.weatherConstraintName))
				.collect(Collectors.toList());

		final List<IUntrimmedAccessData> untrimmedAccessData = accessGenerator.generateUntrimmedAccesses(	accessRequests,
																											new AccessTrimmingConstraintProvider(
																													assetConstraints,
																													constraintsForTrimming));

		final List<UntrimmedAccess> untrimmedAccesses = buildUntrimmedAccesses(untrimmedAccessData);

		for (final IAccessConstraint constraint : constraints) {
			if (constraint.getName()
					.equals(ConstraintUtils.terrainConstraintName)) {
				trimAccessesByTerrain(	untrimmedAccesses,
										geometry,
										assets);
			}
			else if (constraint.getName()
					.equals(ConstraintUtils.weatherConstraintName)) {
				final double maxCloudCover = ((WeatherConstraint) constraint).getBounds()
						.end();
				trimAccessesByWeather(	untrimmedAccesses,
										geometry,
										maxCloudCover);
			}
		}

		return untrimmedAccesses.stream()
				.peek(ua -> ua.setGeometry(geometry))
				.collect(Collectors.toList());
	}

	private Requirement buildRequirement(
			final List<IAccessConstraint> constraints,
			final String sensorType,
			final Geometry geometry,
			final TargetType targetType,
			final OrderOfBattle orderOfBattle ) {

		double minQuality = 0;

		if (constraints != null) {
			for (final IAccessConstraint constraint : constraints) {
				if (constraint.getName()
						.equals(ConstraintUtils.qualityConstraintName)) {
					minQuality = ((QualityConstraint) constraint).min();
				}
			}
		}

		final Requirement req = new Requirement(
				targetType,
				new GeodeticGeometry(
						geometry),
				sensorType,
				minQuality,
				orderOfBattle);

		if (sensorType != null) {
			final SensorTypeConstraint stc = new SensorTypeConstraint();
			stc.setRequiredSensorType(sensorType);
			req.accessConstraints()
					.add(stc);
		}

		return req;
	}

	private List<UntrimmedAccess> buildUntrimmedAccesses(
			final List<IUntrimmedAccessData> untrimmedAccessData ) {

		final List<UntrimmedAccess> returnAccesses = new ArrayList<>();

		for (final IUntrimmedAccessData untrimmedData : untrimmedAccessData) {
			if (untrimmedData.isValid()) {
				final IAccess untrimmedIAccess = untrimmedData.untrimmedAccess();

				final UntrimmedAccess ua = new UntrimmedAccess();
				final List<Access> trimmedAccesses = new ArrayList<>();

				final ISensorMode mode = (ISensorMode) untrimmedIAccess.source();
				ua.setSensorMode(mode.getName());
				ua.setSensorType(mode.sensor()
						.getSensorType());

				final IAsset iAsset = mode.asset();

				ua.setStartTimeISO8601(untrimmedIAccess.startTime()
						.toString());
				ua.setEndTimeISO8601(untrimmedIAccess.endTime()
						.toString());
				ua.setTcaTimeISO8601(untrimmedIAccess.tcaTime()
						.toString());
				ua.setAssetID(String.valueOf(iAsset.id()));
				ua.setAssetName(iAsset.getName());
				ua.setAsset(iAsset);

				if (untrimmedData.validTrimmedAccesses().length == 0) {

					for (final Interval i : untrimmedData.getLostUntrimmedIntervals()) {

						// sample half way through interval
						final DateTime sample = i.getStart()
								.plus(i.toDurationMillis() / 2);

						final List<IConstraintException> exs = untrimmedData
								.getTrimmedConstraintExceptionsAtTime(sample);

						// We are only going to use the FIRST exception as the
						// reason for this interval. Once we process it as the
						// reason, get out of this loop.
						for (final IConstraintException e : exs) {

							if (e instanceof FailedGeometryConstraintException) {
								final FailedGeometryConstraintException ex = (FailedGeometryConstraintException) e;

								logger.debug("Untrimmed access failure: " + ex.constraint()
										.getName() + ": " + ex.getMessage());

								// Only using first one
								ua.setFailureReason(ex.constraint()
										.getName());
								break;
							}
							// The "catch all" of the constraint exceptions
							else if (e instanceof ConstraintException) {
								logger.debug("Untrimmed access failure: " + e.getMessage());

								// Use the message as the reason
								ua.setFailureReason(e.getMessage());
								break;
							}
							else if (e != null) {
								logger.debug("Untrimmed access failure: " + e.getMessage());

								// Use the message as the reason
								ua.setFailureReason(e.getMessage());
								break;
							}
						}
					}
				}

				// get rev/pass info
				int rev = 0;
				int pass = 0;

				if (iAsset instanceof Satellite) {
					final Satellite sat = (Satellite) iAsset;

					// Make sure rev/pass calculator defined
					final IRevAndPassCalculator rpc = sat.getRevAndPassCalculator();
					if (rpc != null) {
						final IRevAndPass revAndPass = rpc.getRevAndPass(untrimmedIAccess.startTime());

						if (revAndPass != null) {
							rev = revAndPass.rev();
							pass = revAndPass.pass();
						}
					}
				}

				ua.setRev(rev);
				ua.setPass(pass);

				for (final IAccess trimmedIAccess : untrimmedData.trimmedAccesses()) {
					if (trimmedIAccess.isValid()) {
						final Access trimmedAccess = new Access();
						trimmedAccess.setStartTimeISO8601(trimmedIAccess.startTime()
								.toString());
						trimmedAccess.setEndTimeISO8601(trimmedIAccess.endTime()
								.toString());
						trimmedAccess.setTcaTimeISO8601(trimmedIAccess.tcaTime()
								.toString());
						trimmedAccesses.add(trimmedAccess);
					}
				}

				ua.setTrimmedAccesses(trimmedAccesses);
				returnAccesses.add(ua);
			}
		}

		return returnAccesses;
	}

	private void generateAssetMission(
			final Asset asset,
			final AdhocMission adhocMission ) {
		final IStateVectorProvider svp = new AircraftTrackStateVectorProvider(
				adhocMission.getWaypoints(),
				new DateTime(
						adhocMission.getOnStationTime()),
				new DateTime(
						adhocMission.getOffStationTime()),
				Length.fromMeters(adhocMission.getAltitudeMeters()));
		asset.setPropagator(svp);

		asset.init();
	}

	public AccessValues getDetailsAtTime(
			final Geometry geometry,
			final DateTime atTime,
			final Asset asset,
			final ISensorMode sensorMode,
			final String propagatorType )
			throws PropagatorTypeDoesNotExistException,
			NoEphemerisFoundException {

		// TODO: Should this handle exceptions or throw them?
		assetClient.updateAssetEphemeris(	asset,
											atTime,
											propagatorType);

		PointToPointGeometry ptp;

		// TODO Area access values not supported yet; centroid will be used
		final Coordinate centroidCoord = geometry.getCentroid()
				.getCoordinate();
		if (!Double.isFinite(centroidCoord.getZ())) {
			centroidCoord.setZ(0.0);
		}

		// centroid is a 2d function, so grab average altitude from the coords
		double alt = 0.0;
		int numZValues = 0;
		for (final Coordinate geomCoord : geometry.getCoordinates()) {
			if (Double.isFinite(geomCoord.getZ())) {
				alt += geomCoord.getZ();
				++numZValues;
			}
		}
		if (numZValues > 0) {
			alt = alt / numZValues;
		}

		final GeodeticPoint point = GeodeticPoint.fromLatLonAlt(Angle.fromRadians(centroidCoord.getY()),
																Angle.fromRadians(centroidCoord.getX()),
																Length.fromMeters(alt));

		ptp = PointToPointGeometry.create(	asset,
											point,
											atTime);

		double quality = 0.0;
		Length gsd = null;

		if (sensorMode != null) {
			final List<Object> qualities = sensorMode.getQualities(ptp);
			if ((qualities != null) && !qualities.isEmpty()) {
				for (final Object o : qualities) {
					final double d = (Double) o;
					if (d > quality) {
						quality = d;
					}
				}
			}

			if (sensorMode.sensor() instanceof IEOSensor) {
				gsd = ((IEOSensor) sensorMode.sensor()).calculateGSD(ptp);
			}
		}

		final AccessValues accessValues = new AccessValues();

		accessValues.setAtTimeISO8601(atTime.toString());

		// Set access details
		accessValues.setAzimuthDeg(ptp.azOffNorth_atDest()
				.degrees());
		accessValues.setElevationDeg(Angle.fromDegrees(90.0)
				.minus(ptp.grazingAngle_atDest())
				.degrees());
		accessValues.setGrazeDeg(ptp.grazingAngle_atDest()
				.degrees());
		accessValues.setQuality(quality);
		if (gsd != null) {
			accessValues.setGsdInches(gsd.in());
		}
		accessValues.setSquintDeg(ptp.squintAngle_atSource()
				.degrees());
		accessValues.setSunAzimuthDeg(ptp.sunAzimuthAngle_atDest()
				.degrees());
		accessValues.setSunElevationDeg(ptp.sunElevationAngle_atDest()
				.degrees());
		accessValues.setSpecularReflectionDeg(ptp.sunSpecularReflectionAngle()
				.degrees());
		accessValues.setMoonElevationDeg(ptp.moonElevationAngle_atDest()
				.degrees());
		accessValues.setMoonAzimuthDeg(ptp.moonAzimuthAngle_atDest()
				.degrees());
		accessValues.setMoonIlluminationPct(ptp.moonIlluminationPct());
		accessValues.setNadirDeg(ptp.nadirAngle_atSource()
				.degrees());
		accessValues.setDopplerConeDeg(ptp.dopplerConeAngle_atSource()
				.degrees());
		accessValues.setCatsAngleDeg(ptp.catsAngle_atSource()
				.degrees());
		accessValues.setSlantRangeMeters(ptp.range()
				.meters());
		accessValues.setAssetPositionECF(new Vector3D(
				ptp.source()
						.geodeticPosition()
						.ecfPosition()
						.x(),
				ptp.source()
						.geodeticPosition()
						.ecfPosition()
						.y(),
				ptp.source()
						.geodeticPosition()
						.ecfPosition()
						.z()));
		accessValues.setTargetPositionECF(new Vector3D(
				ptp.dest()
						.geodeticPosition()
						.ecfPosition()
						.x(),
				ptp.dest()
						.geodeticPosition()
						.ecfPosition()
						.y(),
				ptp.dest()
						.geodeticPosition()
						.ecfPosition()
						.z()));

		return accessValues;
	}

	public AccessValues getAirborneDetailsAtTime(
			final Geometry geometry,
			final DateTime atTime,
			final Asset asset,
			final ISensorMode sensorMode )
			throws NoMissionsFoundException,
			MissionIdDoesNotExistException {

		// TODO: Should this handle exceptions or throw them?
		assetClient.updateAssetMission(	asset,
										atTime,
										null);

		PointToPointGeometry ptp;

		// TODO Area access values not supported yet; centroid will be used
		final Coordinate centroidCoord = geometry.getCentroid()
				.getCoordinate();
		if (!Double.isFinite(centroidCoord.getZ())) {
			centroidCoord.setZ(0.0);
		}

		// centroid is a 2d function, so grab average altitude from the coords
		double alt = 0.0;
		int numZValues = 0;
		for (final Coordinate geomCoord : geometry.getCoordinates()) {
			if (Double.isFinite(geomCoord.getZ())) {
				alt += geomCoord.getZ();
				++numZValues;
			}
		}
		if (numZValues > 0) {
			alt = alt / numZValues;
		}

		final GeodeticPoint point = GeodeticPoint.fromLatLonAlt(Angle.fromRadians(centroidCoord.getY()),
																Angle.fromRadians(centroidCoord.getX()),
																Length.fromMeters(alt));

		ptp = PointToPointGeometry.create(	asset,
											point,
											atTime);

		double quality = 0.0;
		Length gsd = null;

		if (sensorMode != null) {
			final List<Object> qualities = sensorMode.getQualities(ptp);
			if ((qualities != null) && !qualities.isEmpty()) {
				for (final Object o : qualities) {
					final double d = (Double) o;
					if (d > quality) {
						quality = d;
					}
				}
			}

			if (sensorMode.sensor() instanceof IEOSensor) {
				gsd = ((IEOSensor) sensorMode.sensor()).calculateGSD(ptp);
			}
		}

		final AccessValues accessValues = new AccessValues();

		accessValues.setAtTimeISO8601(atTime.toString());

		// Set access details
		accessValues.setAzimuthDeg(ptp.azOffNorth_atDest()
				.degrees());
		accessValues.setElevationDeg(Angle.fromDegrees(90.0)
				.minus(ptp.grazingAngle_atDest())
				.degrees());
		accessValues.setGrazeDeg(ptp.grazingAngle_atDest()
				.degrees());
		accessValues.setQuality(quality);
		if (gsd != null) {
			accessValues.setGsdInches(gsd.in());
		}
		accessValues.setSquintDeg(ptp.squintAngle_atSource()
				.degrees());
		accessValues.setSunAzimuthDeg(ptp.sunAzimuthAngle_atDest()
				.degrees());
		accessValues.setSunElevationDeg(ptp.sunElevationAngle_atDest()
				.degrees());
		accessValues.setSpecularReflectionDeg(ptp.sunSpecularReflectionAngle()
				.degrees());
		accessValues.setMoonElevationDeg(ptp.moonElevationAngle_atDest()
				.degrees());
		accessValues.setMoonAzimuthDeg(ptp.moonAzimuthAngle_atDest()
				.degrees());
		accessValues.setMoonIlluminationPct(ptp.moonIlluminationPct());
		accessValues.setNadirDeg(ptp.nadirAngle_atSource()
				.degrees());
		accessValues.setDopplerConeDeg(ptp.dopplerConeAngle_atSource()
				.degrees());
		accessValues.setCatsAngleDeg(ptp.catsAngle_atSource()
				.degrees());
		accessValues.setSlantRangeMeters(ptp.range()
				.meters());
		accessValues.setAssetPositionECF(new Vector3D(
				ptp.source()
						.geodeticPosition()
						.ecfPosition()
						.x(),
				ptp.source()
						.geodeticPosition()
						.ecfPosition()
						.y(),
				ptp.source()
						.geodeticPosition()
						.ecfPosition()
						.z()));
		accessValues.setTargetPositionECF(new Vector3D(
				ptp.dest()
						.geodeticPosition()
						.ecfPosition()
						.x(),
				ptp.dest()
						.geodeticPosition()
						.ecfPosition()
						.y(),
				ptp.dest()
						.geodeticPosition()
						.ecfPosition()
						.z()));

		return accessValues;
	}

	private void trimAccessesByWeather(
			final List<UntrimmedAccess> untrimmedAccesses,
			final Geometry geometry,
			final double cloudCoverThreshold ) {

		for (final UntrimmedAccess untrimmed : untrimmedAccesses) {
			final Iterator<Access> it = untrimmed.getTrimmedAccesses()
					.iterator();
			final boolean hadAccessesBeforeWeather = it.hasNext();
			while (it.hasNext()) {
				final Access access = it.next();
				final Double cloudCover = serviceClient.getWeatherDuringAccess(	geometry,
																				access.getTcaTimeISO8601());
				if (cloudCover != null) {
					if (cloudCover > cloudCoverThreshold) {
						it.remove();
					}
				}
			}

			if (hadAccessesBeforeWeather && (untrimmed.getTrimmedAccesses()
					.isEmpty())) {
				untrimmed.setFailureReason("Cloud cover");
			}
		}

	}

	private void trimAccessesByTerrain(
			final List<UntrimmedAccess> untrimmedAccesses,
			final Geometry geometry,
			final List<Asset> assets ) {

		for (final UntrimmedAccess untrimmed : untrimmedAccesses) {

			Asset asset = null;
			for (final Asset checkAsset : assets) {
				if (checkAsset.getName()
						.equals(untrimmed.getAssetName())) {
					asset = checkAsset;
				}
			}

			final DateTime untrimmedTCA = new DateTime(
					untrimmed.getTcaTimeISO8601());

			final List<Access> accessesToAdd = new ArrayList<>();

			final Iterator<Access> it = untrimmed.getTrimmedAccesses()
					.iterator();
			final boolean hadAccessesBeforeTerrain = it.hasNext();
			while (it.hasNext()) {
				final Access access = it.next();

				final List<TimeInterval> goodIntervals = serviceClient.getUnmaskedIntervalsFromTerrainMS(	access,
																											geometry,
																											asset);

				if (goodIntervals.size() == 1) {
					access.setStartTimeISO8601(goodIntervals.get(0)
							.getStart()
							.toString());
					access.setEndTimeISO8601(goodIntervals.get(0)
							.getEnd()
							.toString());
				}
				else {

					it.remove();

					for (final TimeInterval interval : goodIntervals) {
						final Access accessToAdd = new Access();
						access.setStartTimeISO8601(interval.getStart()
								.toString());
						access.setEndTimeISO8601(interval.getEnd()
								.toString());
						access.calculateTCA(untrimmedTCA);

						accessesToAdd.add(accessToAdd);
					}
				}
			}

			untrimmed.getTrimmedAccesses()
					.addAll(accessesToAdd);

			Collections.sort(untrimmed.getTrimmedAccesses());

			if (hadAccessesBeforeTerrain && (untrimmed.getTrimmedAccesses()
					.size() == 0)) {
				untrimmed.setFailureReason("Terrain masking");
			}
		}
	}
}
