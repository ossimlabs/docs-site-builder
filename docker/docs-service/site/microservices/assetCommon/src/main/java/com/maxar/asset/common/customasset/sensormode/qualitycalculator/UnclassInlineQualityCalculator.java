package com.maxar.asset.common.customasset.sensormode.qualitycalculator;

import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;

//Lifted from Moonset
public class UnclassInlineQualityCalculator implements
		InlineQualityCalculator
{
	// version for PK
	@Override
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
			final Length slantRange ) {
		System.out.println("calculating inline quality...");
		return 10.0;

	}

	// version for MoonSet
	@Override
	public double getBestQuality(
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
		System.out.println("calculating inline best quality...");
		return 10.0;
	}
}
