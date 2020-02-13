package com.maxar.asset;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.asset.controller.AssetAirborneController;
import com.maxar.asset.controller.AssetSpaceController;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:testassetms.properties")
public class AssetServiceApplicationTest
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		final AssetSpaceController assetSpaceController = applicationContext.getBean(AssetSpaceController.class);

		Assert.assertNotNull(assetSpaceController);

		final AssetAirborneController assetAirborneController = applicationContext
				.getBean(AssetAirborneController.class);

		Assert.assertNotNull(assetAirborneController);

	}
}
