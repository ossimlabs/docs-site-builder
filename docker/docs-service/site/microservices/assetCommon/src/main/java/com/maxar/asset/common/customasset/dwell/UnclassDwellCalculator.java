package com.maxar.asset.common.customasset.dwell;

import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;

/**
 * Unclassified method that returns dwell value based on access' geometry and
 * niirs value. Lifted from moonset.
 *
 * @author ktran date: Aug 28, 2017
 */
public class UnclassDwellCalculator implements
		DwellCalculator
{
	// version for PK
	/**
	 * Gets the dwell.
	 *
	 * @param vehVel
	 *            the vehicle velocity (m/s)
	 * @param bwrf
	 *            the receive bandwidth (Hz)
	 * @param ipr
	 *            the ipr (m)
	 * @param fc
	 *            the center frequency (Hz)
	 * @param azmIPRFac
	 *            the azmimuth IPR broadening factor
	 * @param dca
	 *            the doppler cone angle (rad)
	 * @param slantRange
	 *            the slant range (m)
	 * @return the dwell (s)
	 */
	@Override
	public double getDwell(
			final double vehVel,
			final double bwrf,
			final Length ipr,
			final double fc,
			final double azmIPRFac,
			final Angle dca,
			final Length slantRange ) {

		return 5.0;
	}

	// version for MoonSet
	/**
	 * @param niirs
	 * @param re
	 * @param c
	 * @param k
	 * @param vehAlt
	 * @param vehVel
	 * @param groundRange
	 * @param graze
	 * @param slantRange
	 * @param slopeAngle
	 * @param dca
	 * @param tilt
	 * @param fc
	 * @param nCh
	 * @param antArea
	 * @param bandWidth
	 * @param t0
	 * @param peakPW
	 * @param maxTDTY
	 * @param mnr
	 * @param superRes
	 * @param noiseReduct
	 * @param rngIPRFac
	 * @param azmIPRFac
	 * @param loss
	 * @param nf
	 * @param sigmaC1
	 * @param sigmaC2
	 * @return
	 * @see com.maxar.asset.common.customasset.dwell.DwellCalculator#getDwell(double,
	 *      double, double, double, double, double, double, double, double, double,
	 *      double, double, double, int, double, double, double, double, double,
	 *      double, double, double, double, double, double, double, double, double)
	 *
	 * @author ktran date: Aug 28, 2017
	 */
	@Override
	public double getDwell(
			final double niirs,
			final double re,
			final double c,
			final double k,
			final double vehAlt,
			final double vehVel,
			final double groundRange,
			final double graze,
			final double slantRange,
			final double slopeAngle,
			final double dca,
			final double tilt,
			final double fc,
			final int nCh,
			final double antArea,
			final double bandWidth,
			final double t0,
			final double peakPW,
			final double maxTDTY,
			final double mnr,
			final double superRes,
			final double noiseReduct,
			final double rngIPRFac,
			final double azmIPRFac,
			final double loss,
			final double nf,
			final double sigmaC1,
			final double sigmaC2 ) {

		return 5.0;
	}
}
