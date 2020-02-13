package com.maxar.geometric.intersection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.geometric.intersection.controller.GeometricIntersectionController;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:test-geometricintersectionms.properties")
public class GeometricIntersectionServiceApplicationTest
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		final GeometricIntersectionController geometricIntersectionController = applicationContext
				.getBean(GeometricIntersectionController.class);

		Assert.assertNotNull(geometricIntersectionController);
	}
}
