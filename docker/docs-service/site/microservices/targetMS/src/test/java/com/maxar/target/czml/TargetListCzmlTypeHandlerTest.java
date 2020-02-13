package com.maxar.target.czml;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.target.model.TargetModel;
import com.maxar.target.model.TargetType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testtargetms.properties")
public class TargetListCzmlTypeHandlerTest
{
	@Autowired
	private TargetListCzmlTypeHandler targetListCzmlTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(targetListCzmlTypeHandler.canHandle(TargetModel.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(targetListCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertTrue(targetListCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandle()
			throws ParseException {
		final WKTReader wktReader = new WKTReader();
		final Geometry geometry = wktReader.read("POINT(0.0 0.0)");

		final TargetModel targetModel = TargetModel.builder()
				.targetId("target0")
				.targetName("Target 0")
				.countryCode("ZZ")
				.geometry(geometry)
				.targetType(TargetType.POINT)
				.build();

		final List<String> czmlList = targetListCzmlTypeHandler.handle(Collections.singletonList(targetModel));

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(2,
							czmlList.size());
		Assert.assertTrue(czmlList.get(0)
				.contains("\"name\":\"Requirements\""));
		Assert.assertTrue(czmlList.get(1)
				.contains("\"id\":\"target0:Target 0\""));
		Assert.assertTrue(czmlList.get(1)
				.contains("\"name\":\"target0:Target 0\""));
	}
}
