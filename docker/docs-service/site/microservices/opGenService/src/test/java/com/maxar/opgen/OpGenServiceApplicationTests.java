package com.maxar.opgen;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.opgen.controller.OpGenController;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:testopgen.properties")
public class OpGenServiceApplicationTests
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		final OpGenController controller = applicationContext.getBean(OpGenController.class);

		Assert.assertNotNull(controller);
	}

}
