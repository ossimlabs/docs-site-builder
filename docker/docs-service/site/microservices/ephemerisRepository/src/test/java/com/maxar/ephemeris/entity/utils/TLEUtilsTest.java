package com.maxar.ephemeris.entity.utils;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.maxar.ephemeris.entity.TLE;

public class TLEUtilsTest
{
	private static final int EXAMPLE_SCN = 1;

	private static final int EXAMPLE_SCN_MAPPED = 2;

	private static final String EXAMPLE_TLE_LINE_1_NO_DASH = "1     1U 07041A   19365.16240198  .00000495  00000-0  23031-4 0  9995";

	private static final String EXAMPLE_TLE_LINE_1_MAPPED = "1 00002U 07041A   19365.16240198  .00000495  00000-0  23031-4 0  9995";

	private static final String EXAMPLE_TLE_LINE_1_TO_FILTER = "1     1A 07041A   19365.16240198  .00000495  00000-0  23031-4 0  9995";

	private static final String EXAMPLE_TLE_LINE_1_NO_CLASS = "1     1 07041A   19365.16240198  .00000495  00000-0  23031-4 0  9995";

	private static final String EXAMPLE_TLE_LINE_1_WITH_DASH = "1 00-01U 07041A   19365.16240198  .00000495  00000-0  23031-4 0  9995";

	private static final String EXAMPLE_TLE_LINE_1_INVALID_START = "0 00-01U 07041A   19365.16240198  .00000495  00000-0  23031-4 0  9995";

	private static final String EXAMPLE_TLE_LINE_1_INVALID_LENGTH = "1     1U 07041A   19365.16240198  .00000495  00000-0  230";

	private static final String EXAMPLE_TLE_LINE_2 = "2     1  97.4006  20.5953 0001275 111.0583 249.0791 15.24475338667774";

	private static final String EXAMPLE_TLE_LINE_2_MAPPED = "2 00002  97.4006  20.5953 0001275 111.0583 249.0791 15.24475338667774";

	private static final String EXAMPLE_TLE_LINE_2_INVALID_START = "3     1  97.4006  20.5953 0001275 111.0583 249.0791 15.24475338667774";

	private static final String EXAMPLE_TLE_LINE_2_INVALID_LENGTH = "2     1  97.4006  20.5953 0001275 111.0583 249.0791";

	@Test
	public void testGetSCNFromTleLineOneNoDash() {
		final int scn = TLEUtils.getSCNFromTleLineOne(EXAMPLE_TLE_LINE_1_NO_DASH);

		Assert.assertEquals(EXAMPLE_SCN,
							scn);
	}

	@Test
	public void testGetSCNFromTleLineOneWithDash() {
		final int scn = TLEUtils.getSCNFromTleLineOne(EXAMPLE_TLE_LINE_1_WITH_DASH);

		Assert.assertEquals(EXAMPLE_SCN,
							scn);
	}

	@Test
	public void testParseLinesTLELineOneNoDash() {
		final TLE tle = TLEUtils.parseLinesTLE(	EXAMPLE_TLE_LINE_1_NO_DASH,
												EXAMPLE_TLE_LINE_2,
												'U',
												null);

		Assert.assertNotNull(tle);
		// Note that the SCN isn't set by the parseLinesTLE method.
		Assert.assertEquals(0,
							tle.getScn());
		Assert.assertEquals(EXAMPLE_TLE_LINE_1_NO_DASH,
							tle.getTleLineOne());
		Assert.assertEquals(EXAMPLE_TLE_LINE_2,
							tle.getTleLineTwo());
	}

	@Test
	public void testParseLinesTLELineOneNoDashWithMapping() {
		final TLE tle = TLEUtils.parseLinesTLE(	EXAMPLE_TLE_LINE_1_NO_DASH,
												EXAMPLE_TLE_LINE_2,
												'U',
												Collections.singletonMap(	EXAMPLE_SCN,
																			EXAMPLE_SCN_MAPPED));

		Assert.assertNotNull(tle);
		// Note that the SCN isn't set by the parseLinesTLE method.
		Assert.assertEquals(0,
							tle.getScn());
		Assert.assertEquals(EXAMPLE_TLE_LINE_1_MAPPED,
							tle.getTleLineOne());
		Assert.assertEquals(EXAMPLE_TLE_LINE_2_MAPPED,
							tle.getTleLineTwo());
	}

	@Test
	public void testParseLinesTLELineOneNoDashWithFilter() {
		final TLE tle = TLEUtils.parseLinesTLE(	EXAMPLE_TLE_LINE_1_TO_FILTER,
												EXAMPLE_TLE_LINE_2,
												'U',
												null);

		Assert.assertNotNull(tle);
		// Note that the SCN isn't set by the parseLinesTLE method.
		Assert.assertEquals(0,
							tle.getScn());
		Assert.assertEquals(EXAMPLE_TLE_LINE_1_NO_DASH,
							tle.getTleLineOne());
		Assert.assertEquals(EXAMPLE_TLE_LINE_2,
							tle.getTleLineTwo());
	}

	@Test
	public void testParseLinesTLELineOneNoDashNoClass() {
		final TLE tle = TLEUtils.parseLinesTLE(	EXAMPLE_TLE_LINE_1_NO_CLASS,
												EXAMPLE_TLE_LINE_2,
												'U',
												null);

		Assert.assertNotNull(tle);
		// Note that the SCN isn't set by the parseLinesTLE method.
		Assert.assertEquals(0,
							tle.getScn());
		Assert.assertEquals(EXAMPLE_TLE_LINE_1_NO_DASH,
							tle.getTleLineOne());
		Assert.assertEquals(EXAMPLE_TLE_LINE_2,
							tle.getTleLineTwo());
	}

	@Test
	public void testParseLinesTLELineOneWithDash() {
		final TLE tle = TLEUtils.parseLinesTLE(	EXAMPLE_TLE_LINE_1_WITH_DASH,
												EXAMPLE_TLE_LINE_2,
												'U',
												null);

		Assert.assertNotNull(tle);
		// Note that the SCN isn't set by the parseLinesTLE method.
		Assert.assertEquals(0,
							tle.getScn());
		Assert.assertEquals(EXAMPLE_TLE_LINE_1_WITH_DASH,
							tle.getTleLineOne());
		Assert.assertEquals(EXAMPLE_TLE_LINE_2,
							tle.getTleLineTwo());
	}

	@Test
	public void testParseLinesTLELineOneWithDashWithMapping() {
		final TLE tle = TLEUtils.parseLinesTLE(	EXAMPLE_TLE_LINE_1_WITH_DASH,
												EXAMPLE_TLE_LINE_2,
												'U',
												Collections.singletonMap(	EXAMPLE_SCN,
																			EXAMPLE_SCN_MAPPED));

		Assert.assertNotNull(tle);
		// Note that the SCN isn't set by the parseLinesTLE method.
		Assert.assertEquals(0,
							tle.getScn());
		Assert.assertEquals(EXAMPLE_TLE_LINE_1_MAPPED,
							tle.getTleLineOne());
		Assert.assertEquals(EXAMPLE_TLE_LINE_2_MAPPED,
							tle.getTleLineTwo());
	}

	@Test
	public void testParseLinesTLELineOneInvalidStart() {
		final TLE tle = TLEUtils.parseLinesTLE(	EXAMPLE_TLE_LINE_1_INVALID_START,
												EXAMPLE_TLE_LINE_2,
												'U',
												null);

		Assert.assertNull(tle);
	}

	@Test
	public void testParseLinesTLELineOneInvalidLength() {
		final TLE tle = TLEUtils.parseLinesTLE(	EXAMPLE_TLE_LINE_1_INVALID_LENGTH,
												EXAMPLE_TLE_LINE_2,
												'U',
												null);

		Assert.assertNull(tle);
	}

	@Test
	public void testParseLinesTLELineTwoInvalidStart() {
		final TLE tle = TLEUtils.parseLinesTLE(	EXAMPLE_TLE_LINE_1_NO_DASH,
												EXAMPLE_TLE_LINE_2_INVALID_START,
												'U',
												null);

		Assert.assertNull(tle);
	}

	@Test
	public void testParseLinesTLELineTwoInvalidLength() {
		final TLE tle = TLEUtils.parseLinesTLE(	EXAMPLE_TLE_LINE_1_NO_DASH,
												EXAMPLE_TLE_LINE_2_INVALID_LENGTH,
												'U',
												null);

		Assert.assertNull(tle);
	}

	@Test
	public void testGetLineOneThroughEpochString() {
		final TLE tle = new TLE(
				EXAMPLE_SCN,
				"example",
				EXAMPLE_TLE_LINE_1_NO_DASH,
				EXAMPLE_TLE_LINE_2);

		final String lineOneShortened = TLEUtils.getLineOneThroughEpochString(tle);

		Assert.assertEquals(EXAMPLE_TLE_LINE_1_NO_DASH.substring(	0,
																	32),
							lineOneShortened);
	}

	@Test
	public void testGetLineOneThroughEpochStringNull() {
		final String lineOneShortened = TLEUtils.getLineOneThroughEpochString(null);

		Assert.assertNull(lineOneShortened);
	}
}
