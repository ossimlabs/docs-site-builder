package com.maxar.asset.common.customasset.dwell;

import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;

/**
 * Interface that returns dwell value based on access' geometry and niirs value
 * Lifted from moonset.
 *
 * @author ktran date: Aug 28, 2017
 */
public interface DwellCalculator
{
	// version for PK
	public double getDwell(
			final double vehVel,
			final double bwrf,
			final Length ipr,
			final double fc,
			final double azmIPRFac,
			final Angle dca,
			final Length slantRange );

	// version for MoonSet
	public double getDwell(
			double niirs,
			double re,
			double c,
			double k,
			double vehAlt,
			double vehVel,
			double groundRange,
			double graze,
			double slantRange,
			double slopeAngle,
			double dca,
			double tilt,
			double fc,
			int nCh,
			double antArea,
			double bandWidth,
			double t0,
			double peakPW,
			double maxTDTY,
			double mnr,
			double superRes,
			double noiseReduct,
			double rngIPRFac,
			double azmIPRFac,
			double loss,
			double nf,
			double sigmaC1,
			double sigmaC2 );
}
