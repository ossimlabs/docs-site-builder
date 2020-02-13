package com.maxar.geometric.intersection.czml;

import org.junit.Assert;
import org.junit.Test;

public class AreaOfInterestCzmlPropertiesTest
{
	@Test
	public void testDefaultValues() {
		final AreaOfInterestCzmlProperties areaOfInterestCzmlProperties = new AreaOfInterestCzmlProperties();

		Assert.assertEquals(0.0,
							areaOfInterestCzmlProperties.getOutlineWidth(),
							0.0);
		Assert.assertEquals("FF00FF00",
							areaOfInterestCzmlProperties.getColor());
	}

	@Test
	public void testSetters() {
		final AreaOfInterestCzmlProperties areaOfInterestCzmlProperties = new AreaOfInterestCzmlProperties();
		areaOfInterestCzmlProperties.setOutlineWidth(1.0);
		areaOfInterestCzmlProperties.setColor("FFDD0000");

		Assert.assertEquals(1.0,
							areaOfInterestCzmlProperties.getOutlineWidth(),
							0.0);
		Assert.assertEquals("FFDD0000",
							areaOfInterestCzmlProperties.getColor());
	}
}
