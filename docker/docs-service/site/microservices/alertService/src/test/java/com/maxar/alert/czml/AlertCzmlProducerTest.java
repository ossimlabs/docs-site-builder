package com.maxar.alert.czml;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.alert.model.Event;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-alert.properties")
public class AlertCzmlProducerTest
{
	@Autowired
	private AlertCzmlProducer alertCzmlProducer;

	@Test
	public void testProduceCzmlPoint() {
		final Event event = new Event();
		event.setId("1");
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));
		event.setGeometryWkt("POINT(29.22389757 25.487525689 135.0)");
		event.setCountry("EGY");
		event.setSource("temperature");

		final String czml = alertCzmlProducer.produceCzml(event);

		Assert.assertNotNull(czml);
		Assert.assertTrue(czml.contains("\"id\":\"1\""));
		Assert.assertTrue(czml.contains("\"name\":\"EGY: event0\""));
	}

	@Test
	public void testProduceCzmlPolygon() {
		final Event event = new Event();
		event.setId("1");
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));
		event.setGeometryWkt("POLYGON((29.22389757 25.487525689 135.0, 28.22389757 25.487525689 135.0, "
				+ "28.22389757 24.487525689 135.0, 29.22389757 24.487525689 135.0, 29.22389757 25.487525689 135.0))");
		event.setCountry("EGY");
		event.setSource("temperature");

		final String czml = alertCzmlProducer.produceCzml(event);

		Assert.assertNotNull(czml);
		Assert.assertTrue(czml.contains("\"id\":\"1\""));
		Assert.assertTrue(czml.contains("\"name\":\"EGY: event0\""));
	}

	@Test
	public void testProduceCzmlGeometryWktInvalid() {
		final Event event = new Event();
		event.setId("1");
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));
		event.setGeometryWkt("PINT(29.22389757 25.487525689 135.0)");
		event.setCountry("EGY");
		event.setSource("temperature");

		final String czml = alertCzmlProducer.produceCzml(event);

		Assert.assertNull(czml);
	}
}
