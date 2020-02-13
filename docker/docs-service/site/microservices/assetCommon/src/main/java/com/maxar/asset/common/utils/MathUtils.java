package com.maxar.asset.common.utils;

import com.radiantblue.analytics.core.measures.Length;

public class MathUtils
{
	private MathUtils() {}

	/**
	 * Compute the wavelength from the frequency
	 *
	 * @param freqHz
	 *            the frequency to compute the wavelength of
	 * @return the wavelength
	 */
	public static Length computeWavelengthFromFrequency(
			final double freqHz ) {

		final Length wavelength = Length.fromMeters(PhysicalConstants.SPEED_OF_LIGHT / freqHz);
		return wavelength;
	}

	/**
	 * Convert a raw value to a dB value
	 *
	 * @param rawValue
	 *            the value to convert to dB
	 * @return the converted dB value
	 */
	public static double toDb(
			final double rawValue ) {
		return 10.0 * Math.log10(rawValue);
	}

	/**
	 * Convert a value in dB to a regular (non-dB) value
	 *
	 * @param valueDb
	 *            the vale in dB to be converted
	 * @return the converted non-dB value
	 */
	public static double fromDb(
			final double valueDb ) {
		return Math.pow(10.0,
						valueDb / 10.0);
	}
}
