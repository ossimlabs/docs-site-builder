package com.maxar.ephemeris.entity;

import org.junit.Assert;
import org.junit.Test;

import com.maxar.ephemeris.model.EphemerisModel;
import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.ephemeris.model.TLEModel;

public class TLETest
{
	private static final int EXAMPLE_SCN = 1;

	private static final String EXAMPLE_DESCRIPTION = "example";

	private static final String EXAMPLE_TLE_LINE_1 = "1     1U 07041A   19365.16240198  .00000495  00000-0  23031-4 0  9995";

	private static final String EXAMPLE_TLE_LINE_2 = "2     1  97.4006  20.5953 0001275 111.0583 249.0791 15.24475338667774";

	@Test
	public void testConstructorWithScn() {
		final TLE tle = new TLE(
				EXAMPLE_SCN,
				EXAMPLE_DESCRIPTION,
				EXAMPLE_TLE_LINE_1,
				EXAMPLE_TLE_LINE_2);

		Assert.assertNotNull(tle);
		Assert.assertEquals(EXAMPLE_SCN,
							tle.getScn());
		Assert.assertEquals(EXAMPLE_TLE_LINE_1,
							tle.getTleLineOne());
		Assert.assertEquals(EXAMPLE_TLE_LINE_2,
							tle.getTleLineTwo());
		Assert.assertEquals(EXAMPLE_DESCRIPTION,
							tle.getDescription());
		Assert.assertEquals(EphemerisType.TLE,
							tle.getType());
	}

	@Test
	public void testConstructorWithNoScn() {
		final TLE tle = new TLE(
				EXAMPLE_DESCRIPTION,
				EXAMPLE_TLE_LINE_1,
				EXAMPLE_TLE_LINE_2);

		Assert.assertNotNull(tle);
		Assert.assertEquals(EXAMPLE_SCN,
							tle.getScn());
		Assert.assertEquals(EXAMPLE_TLE_LINE_1,
							tle.getTleLineOne());
		Assert.assertEquals(EXAMPLE_TLE_LINE_2,
							tle.getTleLineTwo());
		Assert.assertEquals(EXAMPLE_DESCRIPTION,
							tle.getDescription());
		Assert.assertEquals(EphemerisType.TLE,
							tle.getType());
	}

	@Test
	public void testCopyConstructor() {
		final TLE tle = new TLE(
				EXAMPLE_SCN,
				EXAMPLE_DESCRIPTION,
				EXAMPLE_TLE_LINE_1,
				EXAMPLE_TLE_LINE_2);

		final TLE tleCopy = new TLE(
				tle);

		Assert.assertNotNull(tleCopy);
		Assert.assertEquals(EXAMPLE_SCN,
							tleCopy.getScn());
		Assert.assertEquals(EXAMPLE_TLE_LINE_1,
							tleCopy.getTleLineOne());
		Assert.assertEquals(EXAMPLE_TLE_LINE_2,
							tleCopy.getTleLineTwo());
		Assert.assertEquals(EXAMPLE_DESCRIPTION,
							tleCopy.getDescription());
		Assert.assertEquals(EphemerisType.TLE,
							tleCopy.getType());
	}

	@Test
	public void testToString() {
		final TLE tle = new TLE(
				EXAMPLE_SCN,
				EXAMPLE_DESCRIPTION,
				EXAMPLE_TLE_LINE_1,
				EXAMPLE_TLE_LINE_2);

		final String tleString = tle.toString();
		Assert.assertTrue(tleString.startsWith("00001"));
		Assert.assertTrue(tleString.endsWith("TLE example"));
	}

	@Test
	public void testToModel() {
		final TLE tle = new TLE(
				EXAMPLE_SCN,
				EXAMPLE_DESCRIPTION,
				EXAMPLE_TLE_LINE_1,
				EXAMPLE_TLE_LINE_2);

		final EphemerisModel ephemerisModel = tle.toModel();

		Assert.assertNotNull(ephemerisModel);
		Assert.assertTrue(ephemerisModel instanceof TLEModel);
	}

	@Test
	public void testEquals() {
		final TLE tle = new TLE(
				EXAMPLE_SCN,
				EXAMPLE_DESCRIPTION,
				EXAMPLE_TLE_LINE_1,
				EXAMPLE_TLE_LINE_2);

		final TLE tleCopy = new TLE(
				tle);

		Assert.assertEquals(tle,
							tleCopy);
	}

	@Test
	public void testHashCode() {
		final TLE tle = new TLE(
				EXAMPLE_SCN,
				EXAMPLE_DESCRIPTION,
				EXAMPLE_TLE_LINE_1,
				EXAMPLE_TLE_LINE_2);

		final TLE tleCopy = new TLE(
				tle);

		final int tleHashCode = tle.hashCode();

		final int tleCopyHashCode = tleCopy.hashCode();

		Assert.assertEquals(tleHashCode,
							tleCopyHashCode);
	}
}
