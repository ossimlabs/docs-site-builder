package com.maxar.weather.czml;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.weather.model.weather.WeatherByDate;
import com.maxar.weather.model.weather.WeatherByGeometry;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class WeatherByDateCzmlTypeHandlerTest
{
	private static final String EXAMPLE_TIME = "2020-01-01T06:00:00Z";

	private static final String EXAMPLE_PARENT_ID = "parent0";

	private static final String EXAMPLE_WKT = "POLYGON((120.0 31.0,120.6 31.0,120.6 30.8,120.0 30.8,120.0 31.0))";

	@Autowired
	private WeatherByDateCzmlTypeHandler weatherByDateCzmlTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(weatherByDateCzmlTypeHandler.canHandle(WeatherByDate.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(weatherByDateCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertFalse(weatherByDateCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandle()
			throws ParseException {
		final WKTReader wktReader = new WKTReader();
		final Geometry geometry = wktReader.read(EXAMPLE_WKT);
		final WeatherByGeometry weatherByGeometry = new WeatherByGeometry(
				0.0,
				geometry);

		final WeatherByDate weatherByDate = new WeatherByDate(
				EXAMPLE_TIME,
				Collections.singletonList(weatherByGeometry),
				EXAMPLE_PARENT_ID);

		final List<String> czmlList = weatherByDateCzmlTypeHandler.handle(weatherByDate);

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(1,
							czmlList.size());
		Assert.assertFalse(czmlList.get(0)
				.isEmpty());
	}
}
