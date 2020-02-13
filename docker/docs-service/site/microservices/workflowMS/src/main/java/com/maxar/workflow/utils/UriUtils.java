package com.maxar.workflow.utils;

import org.springframework.web.util.UriComponentsBuilder;

public class UriUtils
{
	private UriUtils() {}

	public static UriComponentsBuilder putIfNotNullOrEmpty(
			final UriComponentsBuilder builder,
			final String name,
			final String value ) {
		if (value != null && !value.isEmpty()) {
			return builder.queryParam(	name,
										value);
		}
		else {
			return builder;
		}
	}

	public static UriComponentsBuilder putFormatCzml(
			final UriComponentsBuilder builder ) {
		return builder.queryParam(	"format",
									"czml");
	}
}
