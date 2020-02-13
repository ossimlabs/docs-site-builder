package com.maxar.terrain.constraints;

import java.util.List;

import com.maxar.terrain.utils.TerrainMask;
import com.radiantblue.analytics.aerospace.geometry.PointToPointGeometry;
import com.radiantblue.analytics.aerospace.geometry.constraint.IGeometryConstraint;
import com.radiantblue.analytics.core.INamed;
import com.radiantblue.analytics.core.constraint.ConstraintException;
import com.radiantblue.analytics.core.constraint.IAccessConstraint;
import com.radiantblue.analytics.core.constraint.IConstraintException;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

/**
 * Validates access based on a TerrainMask.
 */
public class TerrainMaskConstraint implements
		IGeometryConstraint,
		INamed
{
	private final TerrainMask terrainMask;

	public TerrainMaskConstraint(
			final TerrainMask terrainMask ) {
		this.terrainMask = terrainMask;
	}

	@Override
	public String getName() {
		return "Terrain";
	}

	@Override
	public boolean isCombinable() {
		return false;
	}

	@Override
	public IAccessConstraint combine(
			final List<IAccessConstraint> constraints ) {
		return null;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean isAlwaysTrue() {
		return false;
	}

	@Override
	public double getFailProbability0To1() {
		return 0.50;
	}

	@Override
	public double getLongComputationProbability0to1() {
		return 0.50;
	}

	/**
	 * Check that a source geometry can access a destination geometry with the
	 * TerrainMask in place.
	 *
	 * @param geom
	 *            The source and destination positions.
	 * @return An exception if the TerrainMask prevents the source from accessing
	 *         the destination, or null otherwise.
	 */
	@Override
	public IConstraintException check(
			final PointToPointGeometry geom ) {
		final GeodeticPoint source = geom.source()
				.geodeticPosition();
		final GeodeticPoint dest = geom.dest()
				.geodeticPosition();

		// get azimuth
		final Angle az = geom.azOffNorth_atDest();
		final Angle graze = geom.grazingAngle_atDest();

		final GeodeticPoint sourceGround = GeodeticPoint.fromLatLonAlt(	source.latitude(),
																		source.longitude(),
																		Length.Zero());
		final GeodeticPoint destGround = GeodeticPoint.fromLatLonAlt(	dest.latitude(),
																		dest.longitude(),
																		Length.Zero());

		final Length groundDistance = sourceGround.arcDistance(destGround);

		final Angle minGraze = terrainMask.findMinGraze(az,
														groundDistance);

		// if sensor graze is below min - done
		if (minGraze.radians() > graze.radians()) {
			return new ConstraintException(
					"TerrainConstraint out of bounds");
		}

		// if good, return null
		return null;
	}
}
