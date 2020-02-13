package com.maxar.geometric.intersection.czml;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.geometric.intersection.model.AreaOfInterest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-geometricintersectionms.properties")
public class AreaOfInterestCzmlTypeHandlerTest
{
	private static final String EXAMPLE_ID = "id0";

	private static final String EXAMPLE_WKT = "POINT(0.0 0.0)";

	@Autowired
	private AreaOfInterestCzmlTypeHandler areaOfInterestCzmlTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(areaOfInterestCzmlTypeHandler.canHandle(AreaOfInterest.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(areaOfInterestCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertFalse(areaOfInterestCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandle() {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId(EXAMPLE_ID);
		areaOfInterest.setGeometryWkt(EXAMPLE_WKT);

		final List<String> czmlList = areaOfInterestCzmlTypeHandler.handle(areaOfInterest);

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(1,
							czmlList.size());
		Assert.assertNotNull(czmlList.get(0));
		Assert.assertTrue(czmlList.get(0)
				.contains("\"id\":\"" + EXAMPLE_ID + "\""));
	}
}
