package com.maxar.init.database.utils.ephemeris;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.maxar.ephemeris.model.TLEModel;
import com.maxar.ephemeris.entity.TLE;
import com.maxar.ephemeris.repository.TLERepository;
import com.radiantblue.analytics.mechanics.IStateVectorProvider;
import com.radiantblue.analytics.mechanics.orbit.J2Propagator;
import com.radiantblue.analytics.mechanics.orbit.ThreadSafeSGP4Propagator;
import com.radiantblue.analytics.mechanics.orbit.elementproviders.TLEElementProvider;

@Component
public class InitEphemerisUtils
{
	@Autowired
	private TLERepository tleRepository;

	@Value("${microservices.initdatabase.initplanning.propagatorTypeThresholdDays}")
	private long propagatorTypeThresholdDays;

	public void updateAssetEphemeris(
			final List<com.radiantblue.analytics.isr.core.model.asset.Asset> spaceAssets,
			final DateTime atTime ) {
		for (final com.radiantblue.analytics.isr.core.model.asset.Asset spaceAsset : spaceAssets) {
			final TLE tleRaw = tleRepository
					.findFirstByScnAndEpochMillisLessThanEqualOrderByEpochMillisDesc(	spaceAsset.getId(),
																						atTime.getMillis());

			// if no tle found, use asset's internal tle
			if (tleRaw == null) {
				continue;
			}
			final TLEModel tle = (TLEModel) tleRaw.toModel();

			String propagatorType;
			final long tleAgeMillis = atTime.getMillis() - tle.getEpochMillis();
			final long thresholdMillis = propagatorTypeThresholdDays * 24 * 60 * 60 * 1000;
			if (tleAgeMillis > thresholdMillis) {
				propagatorType = "J2";
			}
			else {
				propagatorType = "SGP4";
			}

			final TLEElementProvider tleProvider = new TLEElementProvider(
					tle.getDescription(),
					tle.getTleLineOne(),
					tle.getTleLineTwo());

			IStateVectorProvider svp = null;

			if (propagatorType.equals("J2")) {
				svp = new J2Propagator(
						tleProvider);
			}
			else if (propagatorType.equals("SGP4")) {
				svp = new ThreadSafeSGP4Propagator(
						tleProvider);
			}
			spaceAsset.setPropagator(svp);
			spaceAsset.init();
		}
	}
}
