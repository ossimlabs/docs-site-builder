package com.maxar.alert.czml;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.alert.model.Event;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-alert.properties")
public class AlertCzmlTypeHandlerTest
{
	@Autowired
	private AlertCzmlTypeHandler alertCzmlTypeHandler;

	@MockBean
	private AlertCzmlProducer alertCzmlProducer;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(alertCzmlTypeHandler.canHandle(Event.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(alertCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertFalse(alertCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandle() {
		Mockito.when(alertCzmlProducer.produceCzml(Mockito.any()))
				.thenReturn("czml");

		final Event event = new Event();
		event.setId("1");
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));
		event.setGeometryWkt("POINT(29.22389757 25.487525689 135.0)");
		event.setCountry("EGY");
		event.setSource("temperature");

		final List<String> czmlList = alertCzmlTypeHandler.handle(event);

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(1,
							czmlList.size());
		Assert.assertEquals("czml",
							czmlList.get(0));
	}
}
