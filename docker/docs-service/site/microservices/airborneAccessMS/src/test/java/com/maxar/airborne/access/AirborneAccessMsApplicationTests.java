package com.maxar.airborne.access;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.airborne.access.controller.AirborneAccessController;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:airborneaccessms.properties")
public class AirborneAccessMsApplicationTests
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		final AirborneAccessController controller = applicationContext.getBean(AirborneAccessController.class);

		Assert.assertNotNull(controller);
	}

}
