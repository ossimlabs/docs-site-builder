package com.maxar.mission.czml;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.mission.model.TrackModel;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testmissionms.properties")
public class TrackCzmlTypeHandlerTest
{
	@Autowired
	private TrackCzmlTypeHandler trackCzmlTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(trackCzmlTypeHandler.canHandle(TrackModel.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(trackCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertFalse(trackCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandle()
			throws ParseException {
		final TrackModel track = new TrackModel();
		final WKTReader wktReader = new WKTReader();
		final Geometry geometry = wktReader.read("LINESTRING(0.0 0.0, 1.0 1.0)");
		track.setTrackGeo(geometry);

		final List<String> czmlList = trackCzmlTypeHandler.handle(track);

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(1,
							czmlList.size());
		Assert.assertTrue(czmlList.get(0)
				.contains("\"id\":\"Track Geo null\""));
		Assert.assertTrue(czmlList.get(0)
				.contains("\"name\":\"Track Geo null\""));
	}
}
