package com.maxar.alert;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.alert.controller.AlertController;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:test-alert.properties")
public class AlertServiceApplicationTest
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		final AlertController alertController = applicationContext.getBean(AlertController.class);

		Assert.assertNotNull(alertController);
	}
}
