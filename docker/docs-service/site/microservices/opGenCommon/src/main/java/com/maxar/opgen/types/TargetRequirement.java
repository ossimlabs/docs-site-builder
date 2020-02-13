package com.maxar.opgen.types;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.maxar.target.model.OrderOfBattle;
import com.maxar.target.model.TargetType;
import com.radiantblue.analytics.core.constraint.IAccessConstraint;
import com.radiantblue.analytics.geodesy.EarthCenteredFrame;
import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;
import com.radiantblue.analytics.isr.core.component.schedule.IScheduleConstraint;
import com.radiantblue.analytics.isr.core.model.requirement.CoupledCollectParameters;
import com.radiantblue.analytics.isr.core.model.requirement.IImintRequirement;
import com.radiantblue.analytics.isr.core.model.requirement.MultiLookParameters;
import com.radiantblue.analytics.isr.core.model.requirement.StereoParameters;
import com.radiantblue.analytics.isr.core.model.requirement.TimeCoincidentParameters;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

public class TargetRequirement implements
		IImintRequirement
{
	private final String name;

	private final GeodeticGeometry geometry;

	public TargetRequirement(
			final String name,
			final GeodeticGeometry geometry ) {
		this.name = name;
		this.geometry = geometry;
	}

	// INamed overrides
	@Override
	public String getName() {
		return name;
	}

	// IHasGeometry overrides
	@Override
	public GeodeticGeometry geometry() {
		return geometry;
	}

	// IStateVectorProvider overrides
	@Override
	public StateVectorsInFrame getStateVectors(
			final DateTime atTime,
			final EarthCenteredFrame frame ) {
		return geometry.centroid()
				.getStateVectors(	atTime,
									frame);
	}

	// IScheduleConstraintProvider overrides
	@Override
	public List<IScheduleConstraint> scheduleConstraints() {
		return Collections.emptyList();
	}

	// IAccessConstraintProvider overrides
	@Override
	public List<IAccessConstraint> accessConstraints() {
		return Collections.emptyList();
	}

	// IRequirement overrides
	@Override
	public List<String> getCollectTypes() {
		return Collections.emptyList();
	}

	@Override
	public Object getTaskingData(
			final String key ) {
		return null;
	}

	// IImintRequirement overrides
	@Override
	public Duration requiredDwellTime() {
		return Duration.ZERO;
	}

	@Override
	public double minQuality(
			final String sensorType,
			final String collectionType ) {
		return 0.0;
	}

	@Override
	public double maxQuality() {
		return 10.0;
	}

	@Override
	public int getPriority() {
		return 99;
	}

	@Override
	public String getTargetType() {
		return TargetType.SITE.name();
	}

	@Override
	public String getOrderOfBattle() {
		return OrderOfBattle.GROUND.name();
	}

	@Override
	public boolean isStereo() {
		return false;
	}

	@Override
	public StereoParameters getStereoParameters() {
		return null;
	}

	@Override
	public boolean isTimeCoincident() {
		return false;
	}

	@Override
	public TimeCoincidentParameters getTimeCoincidentParameters() {
		return null;
	}

	@Override
	public boolean isMultiLook() {
		return false;
	}

	@Override
	public MultiLookParameters getMultiLookParameters() {
		return null;
	}

	@Override
	public boolean isCoupledCollect() {
		return false;
	}

	@Override
	public CoupledCollectParameters getCoupledCollectParameters() {
		return null;
	}

	@Override
	public boolean isSynoptic() {
		return false;
	}
}
