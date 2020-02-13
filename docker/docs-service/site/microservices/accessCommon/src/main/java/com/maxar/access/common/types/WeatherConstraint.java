package com.maxar.access.common.types;

import com.radiantblue.analytics.aerospace.geometry.PointToPointGeometry;
import com.radiantblue.analytics.aerospace.geometry.constraint.DoubleConstraint;
import com.radiantblue.analytics.core.analysis.interval.DoubleInterval;
import com.radiantblue.analytics.core.analysis.interval.Interval;

public class WeatherConstraint extends
		DoubleConstraint
{

	final private double maxCloudCover;

	public WeatherConstraint(
			final double maxCloudCover ) {
		this.maxCloudCover = maxCloudCover;
	}

	@Override
	public String getName() {
		return "Weather";
	}

	@Override
	public Interval<Double> getBounds() {
		return new DoubleInterval(
				0,
				maxCloudCover);
	}

	@Override
	public Double getValue(
			final PointToPointGeometry geom ) {
		// Inside-scheduler use not supported for this constraint
		return null;
	}

}
