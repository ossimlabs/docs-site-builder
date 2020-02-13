package com.maxar.ephemeris.entity.utils;

public class AuxUtils
{
	static String replaceAtEnd(
			final String value,
			final String base ) {
		final String valSafe = (value != null) ? value : "";
		final String baseSafe = (base != null) ? base : "";
		final StringBuilder buffer = new StringBuilder(
				baseSafe);
		final int start = baseSafe.length() - valSafe.length();
		final int end = baseSafe.length();
		if (end > 0) {
			if (start >= 0) {
				buffer.replace(	start,
								end,
								valSafe);
			}
			else {
				final int beginIndex = -start;
				final String valSub = valSafe.substring(beginIndex);
				buffer.replace(	0,
								end,
								valSub);
			}
		}
		final String replaced = buffer.toString();
		return replaced;
	}
}
