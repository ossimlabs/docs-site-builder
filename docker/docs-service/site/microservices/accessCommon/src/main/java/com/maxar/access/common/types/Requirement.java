package com.maxar.access.common.types;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.maxar.target.model.OrderOfBattle;
import com.maxar.target.model.TargetType;
import com.radiantblue.analytics.core.constraint.IAccessConstraint;
import com.radiantblue.analytics.core.constraint.IAccessConstraintProvider;
import com.radiantblue.analytics.geodesy.EarthCenteredFrame;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;
import com.radiantblue.analytics.isr.core.component.accgen.constraint.SensorTypeConstraint;
import com.radiantblue.analytics.isr.core.component.schedule.IScheduleConstraint;
import com.radiantblue.analytics.isr.core.component.schedule.constraint.IHasTarget;
import com.radiantblue.analytics.isr.core.model.requirement.CoupledCollectParameters;
import com.radiantblue.analytics.isr.core.model.requirement.IImintRequirement;
import com.radiantblue.analytics.isr.core.model.requirement.MultiLookParameters;
import com.radiantblue.analytics.isr.core.model.requirement.StereoParameters;
import com.radiantblue.analytics.isr.core.model.requirement.TimeCoincidentParameters;
import com.radiantblue.analytics.isr.core.model.requirement.constraint.QualityConstraint;
import com.radiantblue.analytics.mechanics.IStateVectorProvider;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

import lombok.Data;

@Data
public class Requirement implements
		IImintRequirement,
		IHasTarget,
		IAccessConstraintProvider
{
	private final TargetType targetType;
	private final String sensorType;
	private final GeodeticPoint center;
	private final GeodeticGeometry geometry;
	private final double minQuality;
	private final double maxQuality;
	private final OrderOfBattle orderOfBattle;

	List<IAccessConstraint> accessConstraints = new ArrayList<>();

	public Requirement(
			final TargetType targetType,
			final GeodeticGeometry geometry,
			final String sensorType,
			final double minQuality,
			final OrderOfBattle orderOfBattle) {

		this.targetType = targetType;
		this.geometry = geometry;
		center = geometry.centroid();
		this.sensorType = sensorType;
		this.minQuality = minQuality;
		maxQuality = 10;
		this.orderOfBattle = orderOfBattle;

		updateConstraints();
	}

	public void updateConstraints() {

		accessConstraints.clear();

		if (sensorType != null) {
			accessConstraints
					.add(
							new SensorTypeConstraint(
									sensorType));
		}

		accessConstraints
				.add(
						new QualityConstraint(
								minQuality,
								maxQuality));
	}

	@Override
	public List<String> getCollectTypes() {
		return null;
	}

	@Override
	public Object getTaskingData(
			final String taskingData ) {
		return null;
	}

	@Override
	public List<IAccessConstraint> accessConstraints() {
		return accessConstraints;
	}

	@Override
	public StateVectorsInFrame getStateVectors(
			final DateTime atTime,
			final EarthCenteredFrame frame ) {
		return center
				.getStateVectors(
						atTime,
						frame);
	}

	@Override
	public GeodeticGeometry geometry() {
		return geometry;
	}

	@Override
	public String getName() {
		return "AccessMS";
	}

	@Override
	public CoupledCollectParameters getCoupledCollectParameters() {
		return null;
	}

	@Override
	public MultiLookParameters getMultiLookParameters() {
		return null;
	}

	@Override
	public String getOrderOfBattle() {
		return orderOfBattle.name();
	}

	@Override
	public StereoParameters getStereoParameters() {
		return null;
	}

	@Override
	public String getTargetType() {
		return targetType.name();
	}

	@Override
	public TimeCoincidentParameters getTimeCoincidentParameters() {
		return null;
	}

	@Override
	public boolean isCoupledCollect() {
		return false;
	}

	@Override
	public boolean isMultiLook() {
		return false;
	}

	@Override
	public boolean isStereo() {
		return false;
	}

	@Override
	public boolean isSynoptic() {
		return false;
	}

	@Override
	public boolean isTimeCoincident() {
		return false;
	}

	@Override
	public double maxQuality() {
		return maxQuality;
	}

	@Override
	public double minQuality(
			final String sensorType,
			final String collectionType ) {
		return minQuality;
	}

	@Override
	public Duration requiredDwellTime() {
		return Duration.ZERO;
	}

	@Override
	public IStateVectorProvider target() {
		return center;
	}

	@Override
	public List<IScheduleConstraint> scheduleConstraints() {
		return new ArrayList<>();
	}

	@Override
	public int getPriority() {
		return 0;
	}
}
