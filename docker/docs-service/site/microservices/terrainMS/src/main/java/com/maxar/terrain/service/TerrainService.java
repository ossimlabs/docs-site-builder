package com.maxar.terrain.service;

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.maxar.terrain.constraints.TerrainMaskConstraint;
import com.maxar.terrain.utils.DtedUtils;
import com.maxar.terrain.utils.TerrainMask;
import com.maxar.terrain.model.StateVector;
import com.radiantblue.analytics.aerospace.geometry.PointToPointGeometry;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

/**
 * Responsible for doing the work for the terrain service's endpoints.
 */
@Component
public class TerrainService
{
	@Autowired
	private DtedUtils dtedUtils;

	/**
	 * Get the good time intervals for accessing a target based on terrain.
	 *
	 * Generates a terrain mask for the target using the configured DTED directory,
	 * then creates a TerrainMaskConstraint for that target's terrain mask. The
	 * state vectors will be used to establish which intervals of time the target
	 * can be accessed, based on when the constraint allows for access.
	 *
	 * @param geometry
	 *            The geometry of the target. The center point will be used as the
	 *            target location to generate the terrain mask.
	 * @param stateVectors
	 *            The state vectors of the observer. Accesses will be validated
	 *            based on these points.
	 * @return A list of time intervals where the target can be accessed based on
	 *         the generated terrain mask and the observer's state vectors.
	 */
	public List<Interval> getGoodTimeIntervals(
			final Geometry geometry,
			final List<StateVector> stateVectors ) {
		final Coordinate coordinate = geometry.getNumPoints() == 1 ? geometry.getCoordinate()
				: geometry.getCentroid()
						.getCoordinate();

		final Angle latitude = Angle.fromDegrees(coordinate.getY());
		final Angle longitude = Angle.fromDegrees(coordinate.getX());
		final Length altitude = Length.fromMeters(coordinate.getZ());

		final GeodeticPoint targetGeodeticPoint = GeodeticPoint.fromLatLonAlt(	latitude,
																				longitude,
																				altitude);

		final TerrainMask terrainMask = dtedUtils.generateTerrainMask(targetGeodeticPoint);

		final TerrainMaskConstraint terrainConstraint = new TerrainMaskConstraint(
				terrainMask);

		Deque<Interval> goodIntervals = new LinkedList<>();
		boolean inGoodInterval = false;

		for (final StateVector stateVector : stateVectors) {
			final Angle svLatitude = Angle.fromDegrees(stateVector.getLatitude());
			final Angle svLongitude = Angle.fromDegrees(stateVector.getLongitude());
			final Length svAltitude = Length.fromMeters(stateVector.getAltitude());

			final DateTime atTime = stateVector.getAtTime();

			final GeodeticPoint sourceGeodeticPoint = GeodeticPoint.fromLatLonAlt(	svLatitude,
																					svLongitude,
																					svAltitude);

			final PointToPointGeometry pointToPointGeometry = PointToPointGeometry.create(	sourceGeodeticPoint,
																							targetGeodeticPoint,
																							atTime);

			if (terrainConstraint.check(pointToPointGeometry) == null) {
				if (inGoodInterval) {
					final Interval oldInterval = goodIntervals.pop();
					final Interval newInterval = oldInterval.withEnd(atTime);
					goodIntervals.push(newInterval);
				}
				else {
					final Interval interval = new Interval(
							atTime,
							atTime);
					goodIntervals.push(interval);
					inGoodInterval = true;
				}
			}
			else {
				inGoodInterval = false;
			}
		}

		return goodIntervals.stream()
				.sorted(Comparator.comparing(Interval::getStart)
						.thenComparing(Interval::getEnd))
				.collect(Collectors.toList());
	}
}
