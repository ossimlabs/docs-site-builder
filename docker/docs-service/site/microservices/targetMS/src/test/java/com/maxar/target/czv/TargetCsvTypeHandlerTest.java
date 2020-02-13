package com.maxar.target.czv;

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
public class TargetCsvTypeHandlerTest
{
	@Autowired
	private TargetCsvTypeHandler targetCsvTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(targetCsvTypeHandler.canHandle(TargetModel.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(targetCsvTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHeaders() {
		final String headers = targetCsvTypeHandler.headers();

		Assert.assertNotNull(headers);
		Assert.assertEquals("ID, NAME, DESCRIPTION, CC, GEO REGION, ORDER OF BATTLE, TYPE, GEOMETRY",
							headers);
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

		final List<String> czmlList = targetCsvTypeHandler.handle(targetModel);

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(1,
							czmlList.size());
		Assert.assertEquals("target0, Target 0, , ZZ, null, GROUND, POINT, POINT (0 0)",
							czmlList.get(0));
	}
}
