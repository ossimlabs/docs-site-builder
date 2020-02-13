package com.maxar.asset.common.customasset.sensormode.qualitycalculator;

import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;

/**
 * The Interface InlineQualityCalculator.
 */
//Lifted from Moonset
public interface InlineQualityCalculator
{
	// version for PK
	/**
	 * Gets the quality.
	 *
	 * @param vehVel
	 *            the vehicle velocity (m/s)
	 * @param nCh
	 *            the number of channels or beams
	 * @param antArea
	 *            the antenna area (m^2)
	 * @param bwrf
	 *            the receive bandwidth (Hz)
	 * @param ipr
	 *            the ipr (m)
	 * @param peakPW
	 *            the peak rf power (W)
	 * @param fc
	 *            the center frequency (Hz)
	 * @param maxTDTY
	 *            the maximum transmit duty factor
	 * @param t0
	 *            the system thermal noise temperature (dB)
	 * @param nf
	 *            the system receiver noise figure (dB)
	 * @param loss
	 *            the system receive loss (dB)
	 * @param mnrReq
	 *            the MNR spec (dB)
	 * @param graze
	 *            the grazing angle (rad)
	 * @param slopeAngle
	 *            the slope angle (rad)
	 * @param dca
	 *            the doppler cone angle (rad)
	 * @param slantRange
	 *            the slant range (m)
	 * @return the quality
	 */
	public double getQuality(
			final double vehVel,
			final int nch,
			final double antArea,
			final double bwrf,
			final Length ipr,
			final double peakPW,
			final double fc,
			final double maxTDTY,
			final double t0,
			final double nf,
			final double loss,
			final double mnrReq,
			final Angle graze,
			final Angle slopeAngle,
			final Angle dca,
			final Length slantRange );

	// version for MoonSet
	/**
	 * Gets the best quality.
	 *
	 * @param re
	 *            the re
	 * @param c
	 *            the c
	 * @param k
	 *            the k
	 * @param vehAlt
	 *            the veh alt
	 * @param vehVel
	 *            the veh vel
	 * @param groundRange
	 *            the ground range
	 * @param graze
	 *            the graze
	 * @param slantRange
	 *            the slant range
	 * @param slopeAngle
	 *            the slope angle
	 * @param dca
	 *            the dca
	 * @param tilt
	 *            the tilt
	 * @param fc
	 *            the fc
	 * @param nCh
	 *            the n ch
	 * @param antArea
	 *            the ant area
	 * @param bandWidth
	 *            the band width
	 * @param t0
	 *            the t 0
	 * @param peakPW
	 *            the peak PW
	 * @param maxTDTY
	 *            the max TDTY
	 * @param mnr
	 *            the mnr
	 * @param superRes
	 *            the super res
	 * @param noiseReduct
	 *            the noise reduct
	 * @param rngIPRFac
	 *            the rng IPR fac
	 * @param azmIPRFac
	 *            the azm IPR fac
	 * @param loss
	 *            the loss
	 * @param nf
	 *            the nf
	 * @param sigmaC1
	 *            the sigma C 1
	 * @param sigmaC2
	 *            the sigma C 2
	 * @return the best quality
	 */
	public double getBestQuality(
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
