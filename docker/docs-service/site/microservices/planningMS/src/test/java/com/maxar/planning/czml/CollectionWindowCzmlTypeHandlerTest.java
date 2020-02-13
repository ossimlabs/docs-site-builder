package com.maxar.planning.czml;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.planning.model.image.CollectionWindowModel;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:planningms.properties")
public class CollectionWindowCzmlTypeHandlerTest
{
	@Autowired
	private CollectionWindowCzmlTypeHandler collectionWindowCzmlTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(collectionWindowCzmlTypeHandler.canHandle(CollectionWindowModel.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(collectionWindowCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertFalse(collectionWindowCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandle() {
		final CollectionWindowModel collectionWindowModel = new CollectionWindowModel();
		collectionWindowModel.setCwId("cwid");
		collectionWindowModel.setAssetName("asset0");
		collectionWindowModel.setAssetScn(1);
		collectionWindowModel.setStartMillis(0L);
		collectionWindowModel.setEndMillis(1000L);
		collectionWindowModel.setStatus("status");
		collectionWindowModel.setImageOps(Collections.emptySet());

		final List<String> czmlList = collectionWindowCzmlTypeHandler.handle(collectionWindowModel);

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(1,
							czmlList.size());
		Assert.assertTrue(czmlList.get(0)
				.contains("\"id\":\"CW cwid Asset asset0\""));
		Assert.assertTrue(czmlList.get(0)
				.contains("\"name\":\"CW cwid Asset asset0\""));
	}
}
