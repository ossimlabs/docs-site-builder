package com.maxar.alert.czml;

import org.junit.Assert;
import org.junit.Test;

public class AlertCzmlPropertiesTest
{
	@Test
	public void testLombokData() {
		final AlertCzmlProperties alertCzmlProperties1 = new AlertCzmlProperties();
		alertCzmlProperties1.setPointColor("blue");
		alertCzmlProperties1.setPointPixelSize(0.0);
		alertCzmlProperties1.setPolylineOutlineColor("red");
		alertCzmlProperties1.setPolylineOutlineWidth(1.0);

		Assert.assertEquals("blue",
							alertCzmlProperties1.getPointColor());
		Assert.assertEquals(0.0,
							alertCzmlProperties1.getPointPixelSize(),
							0.0);
		Assert.assertEquals("red",
							alertCzmlProperties1.getPolylineOutlineColor());
		Assert.assertEquals(1.0,
							alertCzmlProperties1.getPolylineOutlineWidth(),
							0.0);

		Assert.assertNotEquals(	alertCzmlProperties1,
								null);

		Assert.assertNotEquals(	alertCzmlProperties1,
								"");

		final AlertCzmlProperties alertCzmlProperties2 = new AlertCzmlProperties();

		Assert.assertNotEquals(	alertCzmlProperties1,
								alertCzmlProperties2);
		Assert.assertNotEquals(	alertCzmlProperties1.hashCode(),
								alertCzmlProperties2.hashCode());
		Assert.assertNotEquals(	alertCzmlProperties1.toString(),
								alertCzmlProperties2.toString());

		alertCzmlProperties2.setPointColor("green");

		Assert.assertNotEquals(	alertCzmlProperties1,
								alertCzmlProperties2);
		Assert.assertNotEquals(	alertCzmlProperties1.hashCode(),
								alertCzmlProperties2.hashCode());

		alertCzmlProperties2.setPointColor("blue");
		alertCzmlProperties2.setPointPixelSize(5.0);

		Assert.assertNotEquals(	alertCzmlProperties1,
								alertCzmlProperties2);
		Assert.assertNotEquals(	alertCzmlProperties1.hashCode(),
								alertCzmlProperties2.hashCode());

		alertCzmlProperties2.setPointPixelSize(0.0);
		alertCzmlProperties2.setPolylineOutlineColor("yellow");

		Assert.assertNotEquals(	alertCzmlProperties1,
								alertCzmlProperties2);
		Assert.assertNotEquals(	alertCzmlProperties1.hashCode(),
								alertCzmlProperties2.hashCode());

		alertCzmlProperties2.setPolylineOutlineColor("red");
		alertCzmlProperties2.setPolylineOutlineWidth(0.0);

		Assert.assertNotEquals(	alertCzmlProperties1,
								alertCzmlProperties2);
		Assert.assertNotEquals(	alertCzmlProperties1.hashCode(),
								alertCzmlProperties2.hashCode());

		alertCzmlProperties2.setPolylineOutlineWidth(1.0);

		Assert.assertEquals(alertCzmlProperties1,
							alertCzmlProperties2);
		Assert.assertEquals(alertCzmlProperties1.hashCode(),
							alertCzmlProperties2.hashCode());
		Assert.assertEquals(alertCzmlProperties1.toString(),
							alertCzmlProperties2.toString());
	}
}
