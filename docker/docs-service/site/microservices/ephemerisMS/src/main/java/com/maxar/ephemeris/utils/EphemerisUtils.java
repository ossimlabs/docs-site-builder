package com.maxar.ephemeris.utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import com.maxar.common.czml.VehiclePositionCZMLUtils;
import com.maxar.ephemeris.entity.Ephemeris;
import com.maxar.ephemeris.model.TLEModel;
import com.radiantblue.analytics.mechanics.IStateVectorProvider;
import com.radiantblue.analytics.mechanics.orbit.J2Propagator;
import com.radiantblue.analytics.mechanics.orbit.ThreadSafeSGP4Propagator;
import com.radiantblue.analytics.mechanics.orbit.elementproviders.TLEElementProvider;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

public class EphemerisUtils
{
	private EphemerisUtils() {}

	// Use 7 days as threshold to choose J2 or SGP4 propagator
	private static final long PROPAGATOR_TYPE_THRESHOLD_MILLIS = 7L * 24L * 60L * 60L * 1000L;

	private static IStateVectorProvider tleToStateVectorProvider(
			final TLEModel tle,
			final DateTime svStart ) {
		final TLEElementProvider tleEp = new TLEElementProvider(
				tle.getDescription(),
				tle.getTleLineOne(),
				tle.getTleLineTwo());

		IStateVectorProvider svp;

		final long tleAgeMillis = svStart.getMillis() - tle.getEpochMillis();
		if (tleAgeMillis > PROPAGATOR_TYPE_THRESHOLD_MILLIS) {
			svp = new J2Propagator(
					tleEp);
		}
		else {
			svp = new ThreadSafeSGP4Propagator(
					tleEp);
		}

		return svp;
	}

	public static List<StateVectorsInFrame> tleToStateVectors(
			final DateTime start,
			final DateTime end,
			final long samplingInterval,
			final TLEModel tle ) {

		final IStateVectorProvider svp = tleToStateVectorProvider(	tle,
																	start);

		return VehiclePositionCZMLUtils.generatePositionVectors(start,
																end,
																samplingInterval,
																svp);
	}

	public static Ephemeris calculateEphemerisTypeByPriorityAndTimingThreshold(
			final List<Ephemeris> ephemerisList,
			final long timingThresholdMillis ) {
		if ((ephemerisList == null) || ephemerisList.isEmpty()) {
			return null;
		}

		if (ephemerisList.size() == 1) {
			return ephemerisList.get(0);
		}

		ephemerisList.sort(Comparator.comparing(Ephemeris::getPriority));

		final Iterator<Ephemeris> ephemerisIterator = ephemerisList.iterator();

		// Loop through the prioritized list of ephemeris types until one is found that
		// is not stale
		Ephemeris ephemerisToReturn = ephemerisIterator.next();

		while (ephemerisIterator.hasNext()) {
			final Ephemeris nextEphemeris = ephemerisIterator.next();

			final DateTime adjustedFirstDateTime = new DateTime(
					ephemerisToReturn.getEpochMillis() + timingThresholdMillis);

			if (adjustedFirstDateTime.isBefore(nextEphemeris.getEpochMillis())) {
				ephemerisToReturn = nextEphemeris;
			}
		}

		return ephemerisToReturn;
	}
}
