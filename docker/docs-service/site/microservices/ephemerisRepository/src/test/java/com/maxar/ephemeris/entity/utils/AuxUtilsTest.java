package com.maxar.ephemeris.entity.utils;

import org.junit.Assert;
import org.junit.Test;

public class AuxUtilsTest
{
	@Test
	public void testReplaceAtEnd() {
		final String result = AuxUtils.replaceAtEnd("xxxx",
													"aaaabbbbcccc");

		Assert.assertEquals("aaaabbbbxxxx",
							result);
	}

	@Test
	public void testReplaceAtEndValueNull() {
		final String result = AuxUtils.replaceAtEnd(null,
													"aaaabbbbcccc");

		Assert.assertEquals("aaaabbbbcccc",
							result);
	}

	@Test
	public void testReplaceAtEndValueEmpty() {
		final String result = AuxUtils.replaceAtEnd(null,
													"aaaabbbbcccc");

		Assert.assertEquals("aaaabbbbcccc",
							result);
	}

	@Test
	public void testReplaceAtEndBaseShorter() {
		final String result = AuxUtils.replaceAtEnd("xxxx",
													"aa");

		Assert.assertEquals("xx",
							result);
	}

	@Test
	public void testReplaceAtEndBaseNull() {
		final String result = AuxUtils.replaceAtEnd("xxxx",
													null);

		Assert.assertEquals("",
							result);
	}

	@Test
	public void testReplaceAtEndBaseEmpty() {
		final String result = AuxUtils.replaceAtEnd("xxxx",
													"");

		Assert.assertEquals("",
							result);
	}
}
