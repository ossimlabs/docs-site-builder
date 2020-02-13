package com.maxar.asset.czml;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.asset.model.AssetSmear;
import com.maxar.asset.model.czml.AssetSmearCzmlProperties;
import com.radiantblue.analytics.isr.core.model.asset.Asset;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testassetms.properties")
public class AssetSmearListCzmlTypeHandlerTest
{
	@Autowired
	private AssetSmearListCzmlTypeHandler assetSmearListCzmlTypeHandler;

	@Autowired
	private AssetSmearCzmlProperties assetSmearCzmlProperties;

	private boolean originalCreateSmearRootNode;

	@Before
	public void setUp() {
		originalCreateSmearRootNode = assetSmearCzmlProperties.isCreateSmearRootNode();
	}

	@After
	public void tearDown() {
		assetSmearCzmlProperties.setCreateSmearRootNode(originalCreateSmearRootNode);
	}

	@Test
	public void testCanHandle() {
		Assert.assertTrue(assetSmearListCzmlTypeHandler.canHandle(AssetSmear.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(assetSmearListCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertTrue(assetSmearListCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandleCreateRootNodeFalse() {
		assetSmearCzmlProperties.setCreateSmearRootNode(false);

		final AssetSmear assetSmear = new AssetSmear();
		assetSmear.setForFramesCzmlRequested(false);
		assetSmear.setOpBeamsCzmlRequested(false);
		assetSmear.setSmearCzmlRequested(false);
		assetSmear.setSensorType("EO");

		final List<String> czmlList = assetSmearListCzmlTypeHandler.handle(Collections.singletonList(assetSmear));

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(1,
							czmlList.size());
		Assert.assertNotNull(czmlList.get(0));
		Assert.assertTrue(czmlList.get(0)
				.isEmpty());
	}

	@Test
	public void testHandleCreateRootNodeTrue() {
		assetSmearCzmlProperties.setCreateSmearRootNode(true);

		final Asset asset = new Asset();
		asset.setName("sample asset");

		final AssetSmear assetSmear = new AssetSmear();
		assetSmear.setForFramesCzmlRequested(false);
		assetSmear.setOpBeamsCzmlRequested(false);
		assetSmear.setSmearCzmlRequested(false);
		assetSmear.setSensorType("EO");
		assetSmear.setAsset(asset);

		final List<String> czmlList = assetSmearListCzmlTypeHandler.handle(Collections.singletonList(assetSmear));

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(2,
							czmlList.size());
		Assert.assertNotNull(czmlList.get(0));
		Assert.assertTrue(czmlList.get(0)
				.contains("\"id\":\"sample asset FOR Smear\""));
		Assert.assertTrue(czmlList.get(0)
				.contains("\"name\":\"sample asset FOR Smear\""));
		Assert.assertNotNull(czmlList.get(1));
		Assert.assertTrue(czmlList.get(1)
				.isEmpty());
	}
}
