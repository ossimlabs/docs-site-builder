package com.maxar.asset.common.aircraft;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import com.maxar.mission.model.MissionModel;
import com.maxar.mission.model.MissionNode;
import com.maxar.mission.model.TrackNodeModel;
import com.radiantblue.analytics.core.Vector3D;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.EarthCenteredFrame;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.mechanics.IStateVectorProvider;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AircraftTrackStateVectorProvider implements
		IStateVectorProvider
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	private List<MissionNode> nodes;

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}
	private DateTime timeOnStation;
	private DateTime timeOffStation;
	private Long lastOffsetMillis;

	public AircraftTrackStateVectorProvider(
			final MissionModel mission ) {

		// get nodes from mission
		nodes = mission.getTrack()
				.getTrackNodes()
				.stream()
				.map(m -> new MissionNode(
						m,
						Length.fromMeters(mission.getAltitudeMeters())))
				.collect(Collectors.toList());

		Collections.sort(nodes);

		timeOnStation = new DateTime(
				mission.getOnStationMillis());
		timeOffStation = new DateTime(
				mission.getOffStationMillis());

		getLastOffset();

		logger.debug("Mission: " + mission.getId() + "/" + mission.getAssetId() + " / TOS: " + timeOnStation + ": "
				+ mission.getAltitudeMeters() + " / " + timeOffStation);
	}

	public AircraftTrackStateVectorProvider(
			final List<TrackNodeModel> adhocNodes,
			final DateTime onStation,
			final DateTime offStation,
			final Length altitude ) {
		nodes = adhocNodes.stream()
				.map(m -> new MissionNode(
						m,
						altitude))
				.collect(Collectors.toList());

		Collections.sort(nodes);

		timeOnStation = onStation;

		timeOffStation = offStation;

		getLastOffset();
	}

	private void getLastOffset() {
		lastOffsetMillis = hasNodes() ? nodes.get(nodes.size() - 1)
				.getOffset()
				.getMillis() : null;
	}

	private boolean hasNodes() {
		return (nodes != null) && !nodes.isEmpty();
	}

	@Override
	public final StateVectorsInFrame getStateVectors(
			final DateTime now,
			final EarthCenteredFrame ecf ) {
		// samples are from [timeOnStation, timeOffStation]
		if ((hasNodes() && now.isBefore(timeOnStation)) || (now.compareTo(timeOffStation) >= 0)
				|| (now.compareTo(timeOnStation.plusMillis(lastOffsetMillis.intValue())) >= 0)) {

			final StateVectorsInFrame svif = new StateVectorsInFrame(
					now,
					// use first or last waypoint - no velocity, no altitude
					now.isBefore(timeOnStation) ? nodes.get(0)
							.getWayPoint()
							.withZeroAltitude()
							.ecfPosition()
							: nodes.get(nodes.size() - 1)
									.getWayPoint()
									.withZeroAltitude()
									.ecfPosition(),
					Vector3D.zero(),
					EarthCenteredFrame.ECEF);

			return svif.toFrame(ecf);
		}

		final PosVel posVel = getPosVelAtTime(now);

		final StateVectorsInFrame svif = new StateVectorsInFrame(
				now,
				posVel.pos.ecfPosition(),
				posVel.vel,
				EarthCenteredFrame.ECEF);

		if (svif.velocity()
				.lengthSquared() < 1E-9) {
			logger.debug("VEL: too low");
		}

		return svif.toFrame(ecf);
	}

	public PosVel getPosVelAtTime(
			final DateTime now ) {

		final PosVel posVel = calcPosAtTime(now);

		return posVel;
	}

	protected PosVel calcPosAtTime(
			final DateTime now ) {

		if (now.isBefore(timeOnStation)) {
			return null;
		}

		if (nodes.size() == 1) {
			return new PosVel(
					nodes.get(0)
							.getWayPoint(),
					Vector3D.zero());
		}

		// find position on track at requested time
		final Duration offset = new Duration(
				timeOnStation,
				now);

		logger.debug("OFFSET: " + offset.getStandardMinutes());

		// find last waypoint since offset
		final Iterator<MissionNode> it = nodes.iterator();
		MissionNode lastNode = null;
		MissionNode nextNode = null;
		MissionNode atNode = null;
		while (it.hasNext()) {

			nextNode = it.next();

			// check for exact time
			if (nextNode.getOffset()
					.isEqual(offset)) {
				atNode = nextNode;

				// if first node is what we were looking for
				// get next and switch
				if (lastNode == null) {
					lastNode = nextNode;

					// safe since always at least two nodes
					nextNode = it.next();
				}
				break;
			}

			// go until we reach next node after offset
			if (nextNode.getOffset()
					.isLongerThan(offset)) {
				break;
			}
			else {
				lastNode = nextNode;
			}
		}

		if (now.isAfter(timeOffStation)) {
			atNode = nodes.get(nodes.size() - 1);
		}

		// calculate position, velocity and altitude

		GeodeticPoint pos = null;

		final Vector3D lastEcf = lastNode.getWayPoint()
				.ecfPosition();
		final Vector3D nextEcf = nextNode.getWayPoint()
				.ecfPosition();

		final Vector3D vDir = nextEcf.$minus(lastEcf)
				.unit();

		// Use TIME to determine what percentage traveled between waypoints

		// time since last way point
		final long sinceOffsetMillis = offset.getMillis() - lastNode.getOffset()
				.getMillis();

		// total time between way points
		final long timeBetweenPointsMillis = nextNode.getOffset()
				.getMillis()
				- lastNode.getOffset()
						.getMillis();

		// percentage traveled from last point
		final double percent;
		if (timeBetweenPointsMillis > 0) {
			percent = (double) sinceOffsetMillis / timeBetweenPointsMillis;
		}
		else {
			percent = 0;
		}

		// total distance between points
		final double totalDistanceMeters = nextEcf.$minus(lastEcf)
				.length();

		// distance traveled since last way point
		final double distanceMeters = totalDistanceMeters * percent;

		final double velEcfMetersPerSecond;
		if (sinceOffsetMillis != 0) {
			// calculate velocity based on distance and time to travel
			velEcfMetersPerSecond = distanceMeters / sinceOffsetMillis / 1000.0;
		}
		else {
			velEcfMetersPerSecond = 0.0;
		}

		final Vector3D velEcf = vDir.times(velEcfMetersPerSecond);

		// if at node we have position
		if (atNode != null) {
			pos = atNode.getWayPoint();
		}
		else {

			final Vector3D posEcf = lastEcf.addAndScale(distanceMeters,
														vDir);

			// allowing attitude to interpolate as well
			pos = GeodeticPoint.fromEcf(posEcf);
		}

		logger.debug("POS: " + now + "/" + pos);

		return new PosVel(
				pos,
				velEcf);
	}

}
