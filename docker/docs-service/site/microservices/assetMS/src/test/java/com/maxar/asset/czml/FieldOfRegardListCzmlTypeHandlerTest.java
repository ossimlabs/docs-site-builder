package com.maxar.asset.czml;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.asset.model.FieldOfRegard;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testassetms.properties")
public class FieldOfRegardListCzmlTypeHandlerTest
{
	@Autowired
	private FieldOfRegardListCzmlTypeHandler fieldOfRegardListCzmlTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(fieldOfRegardListCzmlTypeHandler.canHandle(FieldOfRegard.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(fieldOfRegardListCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertTrue(fieldOfRegardListCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandle() {
		final FieldOfRegard fieldOfRegard = new FieldOfRegard();
		fieldOfRegard.setFieldOfRegardAngleWkt("POINT(0.0 0.0 0.0)");
		fieldOfRegard.setFieldOfRegardAtTime(DateTime.parse("2019-01-01T00:00:00Z"));
		fieldOfRegard.setFieldOfRegardName("field of regard");
		fieldOfRegard.setSensorType("EO");

		final List<String> czmlList = fieldOfRegardListCzmlTypeHandler.handle(Collections.singletonList(fieldOfRegard));

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(2,
							czmlList.size());
		Assert.assertNotNull(czmlList.get(0));
		Assert.assertTrue(czmlList.get(0)
				.contains("\"name\":\"Field Of Regard\""));
		Assert.assertNotNull(czmlList.get(1));
		Assert.assertTrue(czmlList.get(1)
				.contains("\"name\":\"field of regard\""));
	}
}
