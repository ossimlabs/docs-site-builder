package com.maxar.access.common.types;

import java.util.List;

import com.radiantblue.analytics.aerospace.geometry.PointToPointGeometry;
import com.radiantblue.analytics.aerospace.geometry.constraint.IGeometryConstraint;
import com.radiantblue.analytics.core.INamed;
import com.radiantblue.analytics.core.constraint.IAccessConstraint;
import com.radiantblue.analytics.core.constraint.IConstraintException;

public class TerrainConstraint implements
		IGeometryConstraint,
		INamed
{

	@Override
	public String getName() {
		return "Terrain";
	}

	@Override
	public boolean isCombinable() {
		return false;
	}

	@Override
	public IAccessConstraint combine(final List<IAccessConstraint> constraints) {
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

	@Override
	public IConstraintException check(final PointToPointGeometry geom) {
		return null;
	}
}
