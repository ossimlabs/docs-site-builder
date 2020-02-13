package com.maxar.airborne.access.czml;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.access.model.UntrimmedAccess;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:airborneaccessms.properties")
public class AccessCzmlTypeHandlerTest
{
	@Autowired
	private AccessCzmlTypeHandler accessCzmlTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(accessCzmlTypeHandler.canHandle(UntrimmedAccess.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(accessCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertFalse(accessCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandleInvalidStartTime() {
		final UntrimmedAccess untrimmedAccess = new UntrimmedAccess();
		untrimmedAccess.setStartTimeISO8601("invalid");

		final List<String> czmlList = accessCzmlTypeHandler.handle(untrimmedAccess);

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(1,
							czmlList.size());
		Assert.assertNull(czmlList.get(0));
	}
}
