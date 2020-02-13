package com.maxar.ephemeris.entity.utils;

import java.util.Map;

import org.apache.log4j.Logger;

import com.maxar.ephemeris.entity.TLE;
import com.radiantblue.analytics.core.log.SourceLogger;

public class TLEUtils
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	private static final int VALID_LINE_LENGTH = 69;
	private static final int FILLER_LINE_LENGTH = 68;
	private static final int THROUGH_EPOCH_LENGTH = 32;

	public static int getSCNFromTleLineOne(
			final String tleLineOne ) {
		final String scn = tleLineOne.substring(2,
												7)
				.trim();

		final String scnClean;
		if (scn.contains("-")) {
			scnClean = scn.replace(	"-",
									"");
		}
		else {
			scnClean = scn;
		}
		final int scnInt = Integer.parseInt(scnClean);

		return scnInt;
	}

	private static char parseClassFromTleLineOne(
			final String tleLineOne ) {
		final char classChar = tleLineOne.charAt(7);
		return classChar;
	}

	private static String insertClassFillerLineOne(
			final String tleLineOne,
			final char classFiller ) {
		final StringBuilder buffer = new StringBuilder(
				tleLineOne);
		buffer.insert(	7,
						classFiller);
		final String tleLineUpdate = buffer.toString();
		return tleLineUpdate;
	}

	private static String replaceClassLineOne(
			final String tleLineOne,
			final char classification ) {
		final String classString = Character.toString(classification);
		final StringBuilder buffer = new StringBuilder(
				tleLineOne);
		buffer.replace(	7,
						8,
						classString);
		final String tleLineUpdate = buffer.toString();
		return tleLineUpdate;
	}

	private static String replaceScnLine(
			final String tleLine,
			final Integer scn ) {
		final String scnString = makeZeroPaddedScn(scn);
		final StringBuilder buffer = new StringBuilder(
				tleLine);
		buffer.replace(	2,
						7,
						scnString);
		final String tleLineUpdate = buffer.toString();
		return tleLineUpdate;
	}

	public static TLE parseLinesTLE(
			final String tleLineOne,
			final String tleLineTwo,
			final char classFiller,
			final Map<Integer, Integer> scnAliases ) {

		if (!isValidLineOneSimpleParseCheck(tleLineOne) || !isValidLineTwoSimpleParseCheck(tleLineTwo)) {
			logger.warn("TLE improperly formatted: 1[" + tleLineOne + "] 2[" + tleLineTwo + "]");
			return null;
		}

		final TLE tle = new TLE();
		tle.setTleLineOne(tleLineOne);
		tle.setTleLineTwo(tleLineTwo);

		filterClassFiller(	tle,
							classFiller);
		filterClassValue(	tle,
							classFiller);
		filterScnAlias(	tle,
						scnAliases);

		if (!isValidSimpleCheck(tle)) {
			logger.warn("TLE improperly formatted: 1[" + tleLineOne + "] 2[" + tleLineTwo + "]");
			return null;
		}

		return tle;
	}

	private static void filterClassFiller(
			final TLE tle,
			final char classFiller ) {
		final boolean testLineOneFiller = isLineOneStart(tle.getTleLineOne())
				&& isValidLineMissingFillerLength(tle.getTleLineOne());
		if (testLineOneFiller) {
			final String tleLineOneUpdate = insertClassFillerLineOne(	tle.getTleLineOne(),
																		classFiller);
			tle.setTleLineOne(tleLineOneUpdate);
		}
	}

	private static void filterClassValue(
			final TLE tle,
			final char classFiller ) {
		final char classification = parseClassFromTleLineOne(tle.getTleLineOne());
		if ((classification != 'U') && (classification != 'S')) {
			final String tleLineOneUpdate = replaceClassLineOne(tle.getTleLineOne(),
																classFiller);
			tle.setTleLineOne(tleLineOneUpdate);
		}
	}

	private static void filterScnAlias(
			final TLE tle,
			final Map<Integer, Integer> scnAliases ) {
		if ((scnAliases != null) && !scnAliases.isEmpty()) {
			final int scn = getSCNFromTleLineOne(tle.getTleLineOne());
			final Integer keyScn = scn;
			final Integer valScn = scnAliases.get(keyScn);
			if (valScn != null) {
				final String tleLineOneUpdate = replaceScnLine(	tle.getTleLineOne(),
																valScn);
				final String tleLineTwoUpdate = replaceScnLine(	tle.getTleLineTwo(),
																valScn);
				tle.setTleLineOne(tleLineOneUpdate);
				tle.setTleLineTwo(tleLineTwoUpdate);
			}
		}
	}

	private static boolean isValidSimpleCheck(
			final TLE tle ) {
		if (tle != null) {
			final String line1 = tle.getTleLineOne();
			final String line2 = tle.getTleLineTwo();
			return isLineOneStart(line1) && isLineTwoStart(line2) && isValidLineLength(line1)
					&& isValidLineLength(line2);
		}
		else {
			return false;
		}
	}

	private static boolean isLineOneStart(
			final String line ) {
		final boolean test = (line != null) && line.startsWith("1 ");
		return test;
	}

	private static boolean isLineTwoStart(
			final String line ) {
		final boolean test = (line != null) && line.startsWith("2 ");
		return test;
	}

	private static boolean isValidLineLength(
			final String line ) {
		final boolean test = (line != null) && (line.length() == VALID_LINE_LENGTH);
		return test;
	}

	private static boolean isValidLineMissingFillerLength(
			final String line ) {
		final boolean test = (line != null) && (line.length() == FILLER_LINE_LENGTH);
		return test;
	}

	public static boolean isValidLineOneSimpleParseCheck(
			final String line ) {
		final boolean test = isLineOneStart(line) && (isValidLineLength(line) || isValidLineMissingFillerLength(line));
		return test;
	}

	public static boolean isValidLineTwoSimpleParseCheck(
			final String line ) {
		final boolean test = isLineTwoStart(line) && isValidLineLength(line);
		return test;
	}

	private static String makeZeroPaddedScn(
			final Integer scn ) {
		final String base = "00000";
		final String value = scn.toString();
		final String padded = AuxUtils.replaceAtEnd(value,
													base);
		return padded;
	}

	public static String getLineOneThroughEpochString(
			final TLE tle ) {
		final String str;
		if ((tle != null) && (tle.getTleLineOne()
				.length() >= THROUGH_EPOCH_LENGTH)) {
			str = tle.getTleLineOne()
					.substring(	0,
								THROUGH_EPOCH_LENGTH);
		}
		else {
			str = null;
		}
		return str;
	}

}
