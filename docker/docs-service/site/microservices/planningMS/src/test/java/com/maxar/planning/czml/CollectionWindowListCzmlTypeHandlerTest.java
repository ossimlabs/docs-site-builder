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
public class CollectionWindowListCzmlTypeHandlerTest
{
	@Autowired
	private CollectionWindowListCzmlTypeHandler collectionWindowListCzmlTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(collectionWindowListCzmlTypeHandler.canHandle(CollectionWindowModel.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(collectionWindowListCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertTrue(collectionWindowListCzmlTypeHandler.handlesIterable());
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

		final List<String> czmlList = collectionWindowListCzmlTypeHandler
				.handle(Collections.singletonList(collectionWindowModel));

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(2,
							czmlList.size());
		Assert.assertTrue(czmlList.get(0)
				.contains("\"name\":\"Collection Windows\""));
		Assert.assertTrue(czmlList.get(1)
				.contains("\"id\":\"CW cwid Asset asset0\""));
		Assert.assertTrue(czmlList.get(1)
				.contains("\"name\":\"CW cwid Asset asset0\""));
	}
}
