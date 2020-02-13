package com.maxar.spaceobjectcatalog;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.spaceobjectcatalog.controller.SpaceObjectCatalogController;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:test-space-object-catalog.properties")
public class SpaceObjectCatalogServiceApplicationTest
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		final SpaceObjectCatalogController spaceObjectCatalogController = applicationContext
				.getBean(SpaceObjectCatalogController.class);

		Assert.assertNotNull(spaceObjectCatalogController);
	}
}
