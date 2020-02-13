package com.maxar.target;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.target.controller.TargetController;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:testtargetms.properties")
public class TargetMsApplicationTests
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		final TargetController targetController = applicationContext.getBean(TargetController.class);

		Assert.assertNotNull(targetController);
	}
}
