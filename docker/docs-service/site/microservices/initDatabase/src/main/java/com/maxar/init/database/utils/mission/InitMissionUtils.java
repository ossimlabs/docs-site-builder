package com.maxar.init.database.utils.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.maxar.asset.entity.Asset;
import com.maxar.asset.entity.AssetType;
import com.maxar.asset.repository.AssetRepository;
import com.maxar.mission.entity.Mission;
import com.maxar.mission.entity.Track;
import com.maxar.mission.entity.TrackNode;
import com.maxar.mission.repository.MissionRepository;
import com.maxar.mission.repository.TrackRepository;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

@Component
public class InitMissionUtils
{
	public static Random randomGenerator = new Random(
			8675309);

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private MissionRepository missionRepository;

	@Autowired
	private TrackRepository trackRepository;

	@Value("${microservices.initdatabase.initmission.minlatitude}")
	private double minLatitudeDegrees;

	@Value("${microservices.initdatabase.initmission.maxlatitude}")
	private double maxLatitudeDegrees;

	@Value("${microservices.initdatabase.initmission.minlongitude}")
	private double minLongitudeDegrees;

	@Value("${microservices.initdatabase.initmission.maxlongitude}")
	private double maxLongitudeDegrees;

	@Value("${microservices.initdatabase.initmission.minnumwaypoints}")
	private int minNumWayPoints;

	@Value("${microservices.initdatabase.initmission.maxnumwaypoints}")
	private int maxNumWayPoints;

	@Value("${microservices.initdatabase.initmission.minaltitudemeters}")
	private double minAltitudeMeters;

	@Value("${microservices.initdatabase.initmission.maxaltitudemeters}")
	private double maxAltitudeMeters;

	@Value("${microservices.initdatabase.initmission.minspeedmph}")
	private double minSpeedMph;

	@Value("${microservices.initdatabase.initmission.maxspeedmph}")
	private double maxSpeedMph;

	@Value("${microservices.initdatabase.initmission.mininitialazimuthdegrees}")
	private double minInitialAzimuthDegrees;

	@Value("${microservices.initdatabase.initmission.maxinitialazimuthdegrees}")
	private double maxInitialAzimuthDegrees;

	@Value("${microservices.initdatabase.initmission.minazimuthchangedegrees}")
	private double minAzimuthChangedDegrees;

	@Value("${microservices.initdatabase.initmission.maxazimuthchangedegrees}")
	private double maxAzimuthChangedDegrees;

	@Value("${microservices.initdatabase.initmission.minsegmentlengthmiles}")
	private double minSegmentLengthMiles;

	@Value("${microservices.initdatabase.initmission.maxsegmentlengthmiles}")
	private double maxSegmentLengthMiles;

	static private GeometryFactory geomFactory = new GeometryFactory();

	public List<Asset> getAirborneAssets() {
		final List<Asset> assets = assetRepository.getByType(AssetType.AIRBORNE)
				.stream()
				.collect(Collectors.toList());

		return assets;
	}

	public void generateRandomMission(
			final int index,
			final DateTime beginTime,
			final DateTime endTime,
			final String assetName ) {

		final double speedMph = getRandomSpeedMph();
		final Track track = generateTrack(	index,
											speedMph);
		final String missionId = "MISSION_" + index;
		final String missionName = "MISSION " + index;

		DateTime missionStart = getRandomMissionStartTime(	beginTime,
															endTime);
		DateTime missionStop = getMissionStopTime(	missionStart,
													track);

		// adjust mission if it runs past endTime
		// this could theoretically be legit, so if it is past end time, just make it
		// end at endtime
		// even if it pushes missionStart before beginTime
		if (missionStop.isAfter(endTime)) {
			final long adjustTimeMillis = missionStop.getMillis() - endTime.getMillis();
			missionStop = missionStop.minus(adjustTimeMillis);
			missionStart = missionStart.minus(adjustTimeMillis);
		}

		final Length altitude = getRandomAltitude();

		final Mission mission = new Mission(
				missionId,
				missionName,
				assetName,
				track,
				missionStart.getMillis(),
				missionStop.getMillis(),
				speedMph,
				altitude.meters());

		missionRepository.saveAndFlush(mission);
	}

	private Track generateTrack(
			final int id,
			final double speedMph ) {
		GeodeticPoint geodeticPoint = getRandomStartPoint();
		Angle azimuth = getRandomInitialAzimuth();
		final int numWayPoints = getRandomNumWaypoints();

		final String trackId = "TRACK_" + id;
		final String trackName = "TRACK " + id;
		final Track track = new Track(
				trackId,
				trackName);

		final List<TrackNode> wayPoints = new ArrayList<>();
		final List<Coordinate> trackCoords = new ArrayList<>();

		int seq = 1;

		Coordinate coord = new Coordinate(
				geodeticPoint.longitude()
						.degrees(),
				geodeticPoint.latitude()
						.degrees());
		Point point = convertCoordinateToPoint(coord);
		trackCoords.add(coord);

		long totalOffsetMillis = 0;

		wayPoints.add(new TrackNode(
				track,
				seq++,
				point,
				totalOffsetMillis,
				true));

		while (seq <= numWayPoints) {
			// get random length
			final Length segmentLength = getRandomLength();

			// move point according to initial azimuth and random length
			GeodeticPoint movedPoint = geodeticPoint.move(	azimuth,
															segmentLength);
			// Recalculate point if outside min/max lat/lon
			while ((movedPoint.latitude()
					.degrees() < minLatitudeDegrees)
					|| (movedPoint.latitude()
							.degrees() > maxLatitudeDegrees)
					|| (movedPoint.longitude()
							.degrees() < minLongitudeDegrees)
					|| (movedPoint.longitude()
							.degrees() > maxLongitudeDegrees)) {
				azimuth = azimuth.$plus(getRandomAzimuthChange());
				movedPoint = geodeticPoint.move(azimuth,
												segmentLength);
			}
			geodeticPoint = movedPoint;

			// calculate offset based upon speed
			final long offsetMillis = (long) ((segmentLength.mi() / speedMph) * 60 * 60 * 1000);
			totalOffsetMillis += offsetMillis;

			// create new waypoint
			coord = new Coordinate(
					geodeticPoint.longitude()
							.degrees(),
					geodeticPoint.latitude()
							.degrees());
			trackCoords.add(coord);
			point = convertCoordinateToPoint(coord);
			wayPoints.add(new TrackNode(
					track,
					seq++,
					point,
					totalOffsetMillis,
					true));

			// calculate next azimuth
			azimuth = azimuth.$plus(getRandomAzimuthChange());
		}

		// set track waypoints and geometry
		track.setTrackNodes(wayPoints);
		final CoordinateArraySequence trackCoordArray = new CoordinateArraySequence(
				trackCoords.stream()
						.toArray(Coordinate[]::new));

		final Geometry trackLine = new LineString(
				trackCoordArray,
				geomFactory);
		track.setTrackGeo(trackLine);

		trackRepository.saveAndFlush(track);

		return track;
	}

	private Angle getRandomAzimuthChange() {
		final double azimuthDegrees = minAzimuthChangedDegrees
				+ (randomGenerator.nextDouble() * (maxAzimuthChangedDegrees - minAzimuthChangedDegrees));
		final double multiplier = (randomGenerator.nextDouble() >= 0.5 ? 1.0 : -1.0);

		return Angle.fromDegrees(azimuthDegrees * multiplier);
	}

	private Point convertCoordinateToPoint(
			final Coordinate coord ) {

		final Coordinate[] coordArray = {
			coord
		};

		final CoordinateArraySequence cas = new CoordinateArraySequence(
				coordArray);

		final Point point = new Point(
				cas,
				geomFactory);
		return point;
	}

	private DateTime getRandomMissionStartTime(
			final DateTime beginTime,
			final DateTime endTime ) {
		final long beginMillis = beginTime.getMillis();
		final long endMillis = endTime.getMillis();

		final long startMillis = beginMillis + (long) (randomGenerator.nextDouble() * (endMillis - beginMillis));

		return new DateTime(
				startMillis);
	}

	private DateTime getMissionStopTime(
			final DateTime missionStart,
			final Track track ) {
		final Duration missionDuration = new Duration(
				track.getTrackNodes()
						.get(track.getTrackNodes()
								.size() - 1)
						.getOffsetMillis());

		return missionStart.plus(missionDuration);
	}

	private Length getRandomLength() {
		final double lengthMiles = minSegmentLengthMiles
				+ (randomGenerator.nextDouble() * (maxSegmentLengthMiles = minSegmentLengthMiles));

		return Length.fromMiles(lengthMiles);
	}

	private GeodeticPoint getRandomStartPoint() {
		final double latDegrees = minLatitudeDegrees
				+ (randomGenerator.nextDouble() * (maxLatitudeDegrees - minLatitudeDegrees));
		final double lonDegrees = minLongitudeDegrees
				+ (randomGenerator.nextDouble() * (maxLongitudeDegrees - minLongitudeDegrees));
		final Angle lat = Angle.fromDegrees(latDegrees);
		final Angle lon = Angle.fromDegrees(lonDegrees);

		return GeodeticPoint.fromLatLon(lat,
										lon);
	}

	private Angle getRandomInitialAzimuth() {
		final double initialAzimuthDegrees = minInitialAzimuthDegrees
				+ (randomGenerator.nextDouble() * (maxInitialAzimuthDegrees - minInitialAzimuthDegrees));

		return Angle.fromDegrees(initialAzimuthDegrees);
	}

	private int getRandomNumWaypoints() {
		final int numWayPoints = minNumWayPoints
				+ (int) (randomGenerator.nextDouble() * (maxNumWayPoints - minNumWayPoints));

		return numWayPoints;
	}

	private double getRandomSpeedMph() {
		final double speedMph = minSpeedMph + (randomGenerator.nextDouble() * (maxSpeedMph - minSpeedMph));

		return speedMph;
	}

	private Length getRandomAltitude() {
		final double altitudeMeters = minAltitudeMeters
				+ (randomGenerator.nextDouble() * (maxAltitudeMeters - minAltitudeMeters));

		return Length.fromMeters(altitudeMeters);
	}
}
