package com.maxar.asset.common.aircraft;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.common.collect.Ordering;
import com.radiantblue.analytics.isr.component.riseset.SubGeoRiseSetGenerator;
import com.radiantblue.analytics.isr.core.component.riseset.IRiseSet;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.mechanics.IStateVectorProvider;

public class AircraftRiseSetGenerator extends
		SubGeoRiseSetGenerator
{
	@Override
	public boolean canHandle(
			final IStateVectorProvider source,
			final IStateVectorProvider dest,
			final DateTime startTime,
			final DateTime endTime ) {
		return getTrackProvider(source) != null;
	}

	@Override
	public List<IRiseSet> generateRiseSets(
			final IStateVectorProvider source,
			final IStateVectorProvider dest,
			final DateTime startTime,
			final DateTime endTime ) {

		final AircraftTrackStateVectorProvider aircraft = getTrackProvider(source);

		if (!checkOverlap(	startTime,
							endTime,
							aircraft)) {
			return Collections.emptyList();
		}

		// Set start to the latest of startTime, or onStationTime
		final DateTime start = Ordering.natural()
				.max(	startTime,
						aircraft.getTimeOnStation());
		// Set end to the earliest of endTime or offStationTime
		final DateTime end = Ordering.natural()
				.min(	endTime,
						aircraft.getTimeOffStation());

		return super.generateRiseSets(	source,
										dest,
										start,
										end);
	}

	private boolean checkOverlap(
			final DateTime startTime,
			final DateTime endTime,
			final AircraftTrackStateVectorProvider aircraft ) {
		return new Interval(
				startTime,
				endTime).overlaps(new Interval(
						aircraft.getTimeOnStation(),
						aircraft.getTimeOffStation()));
	}

	private AircraftTrackStateVectorProvider getTrackProvider(
			final IStateVectorProvider source ) {
		if (source instanceof Asset) {
			return getTrackProvider(((Asset) source).getPropagator());
		}
		else if (source instanceof AircraftTrackStateVectorProvider) {
			return (AircraftTrackStateVectorProvider) source;
		}
		else {
			return null;
		}
	}
}
