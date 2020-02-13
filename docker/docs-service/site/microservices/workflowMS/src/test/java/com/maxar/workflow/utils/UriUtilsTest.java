package com.maxar.workflow.utils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

public class UriUtilsTest
{
	private static final String EXAMPLE_URL = "http://localhost:8080/path";

	private static final String EXAMPLE_NAME = "name";

	private static final String EXAMPLE_VALUE = "value";

	@Test
	public void testPutIfNotNullOrEmptyIsNull() {
		final UriComponentsBuilder builder = UriUtils.putIfNotNullOrEmpty(	UriComponentsBuilder.fromHttpUrl(EXAMPLE_URL),
																			EXAMPLE_NAME,
																			null);

		Assert.assertEquals(EXAMPLE_URL,
							builder.toUriString());
	}

	@Test
	public void testPutIfNotNullOrEmptyIsEmpty() {
		final UriComponentsBuilder builder = UriUtils.putIfNotNullOrEmpty(	UriComponentsBuilder.fromHttpUrl(EXAMPLE_URL),
																			EXAMPLE_NAME,
																			"");

		Assert.assertEquals(EXAMPLE_URL,
							builder.toUriString());
	}

	@Test
	public void testPutIfNotNullOrEmptyIsNotNullOrEmpty() {
		final UriComponentsBuilder builder = UriUtils.putIfNotNullOrEmpty(	UriComponentsBuilder.fromHttpUrl(EXAMPLE_URL),
																			EXAMPLE_NAME,
																			EXAMPLE_VALUE);

		Assert.assertEquals(EXAMPLE_URL + "?" + EXAMPLE_NAME + "=" + EXAMPLE_VALUE,
							builder.toUriString());
	}

	@Test
	public void testPutFormatCzml() {
		final UriComponentsBuilder builder = UriUtils.putFormatCzml(UriComponentsBuilder.fromHttpUrl(EXAMPLE_URL));

		Assert.assertEquals(EXAMPLE_URL + "?format=czml",
							builder.toUriString());
	}

	@Test
	public void testPutMultiple() {
		final UriComponentsBuilder builder = UriUtils
				.putFormatCzml(UriUtils.putIfNotNullOrEmpty(UriComponentsBuilder.fromHttpUrl(EXAMPLE_URL),
															EXAMPLE_NAME,
															EXAMPLE_VALUE));

		Assert.assertEquals(EXAMPLE_URL + "?" + EXAMPLE_NAME + "=" + EXAMPLE_VALUE + "&format=czml",
							builder.toUriString());
	}
}
