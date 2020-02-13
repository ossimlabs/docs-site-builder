package com.maxar.access.czml;

import java.util.Collections;
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
@TestPropertySource(locations = "classpath:testaccessms.properties")
public class AccessListCzmlTypeHandlerTest
{
	@Autowired
	private AccessListCzmlTypeHandler accessListCzmlTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(accessListCzmlTypeHandler.canHandle(UntrimmedAccess.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(accessListCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertTrue(accessListCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandleInvalidStartTime() {
		final UntrimmedAccess untrimmedAccess = new UntrimmedAccess();
		untrimmedAccess.setStartTimeISO8601("invalid");

		final List<String> czmlList = accessListCzmlTypeHandler.handle(Collections.singletonList(untrimmedAccess));

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(2,
							czmlList.size());
		Assert.assertTrue(czmlList.get(0)
				.contains("\"id\":\"Space Accesses\""));
		Assert.assertTrue(czmlList.get(0)
				.contains("\"name\":\"Space Accesses\""));
		Assert.assertNull(czmlList.get(1));
	}
}
