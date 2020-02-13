package com.maxar.geometric.intersection.czml;

import org.junit.Assert;
import org.junit.Test;

import com.maxar.geometric.intersection.model.AreaOfInterest;

public class AreaOfInterestCzmlProducerTest
{
	private static final String EXAMPLE_ID = "id0";

	private static final String EXAMPLE_WKT = "POINT(0.0 0.0)";

	private static final String EXAMPLE_WKT_INVALID = "OINT(0.0 0.0)";

	@Test
	public void testProduceCzml() {
		final AreaOfInterestCzmlProducer areaOfInterestCzmlProducer = new AreaOfInterestCzmlProducer();

		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId(EXAMPLE_ID);
		areaOfInterest.setGeometryWkt(EXAMPLE_WKT);

		final AreaOfInterestCzmlProperties areaOfInterestCzmlProperties = new AreaOfInterestCzmlProperties();

		final String czml = areaOfInterestCzmlProducer.produceCzml(	areaOfInterest,
																	areaOfInterestCzmlProperties);

		Assert.assertNotNull(czml);
		Assert.assertTrue(czml.contains("\"id\":\"" + EXAMPLE_ID + "\""));
	}

	@Test
	public void testProduceCzmlWktInvalid() {
		final AreaOfInterestCzmlProducer areaOfInterestCzmlProducer = new AreaOfInterestCzmlProducer();

		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId(EXAMPLE_ID);
		areaOfInterest.setGeometryWkt(EXAMPLE_WKT_INVALID);

		final AreaOfInterestCzmlProperties areaOfInterestCzmlProperties = new AreaOfInterestCzmlProperties();

		final String czml = areaOfInterestCzmlProducer.produceCzml(	areaOfInterest,
																	areaOfInterestCzmlProperties);

		Assert.assertNull(czml);
	}
}
