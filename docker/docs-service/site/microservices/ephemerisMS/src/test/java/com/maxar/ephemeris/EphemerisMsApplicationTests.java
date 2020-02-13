package com.maxar.ephemeris;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.ephemeris.controller.EphemerisController;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource("classpath:testephemerisms.properties")
public class EphemerisMsApplicationTests
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		Assert.assertNotNull(applicationContext);

		final EphemerisController ephemerisController = applicationContext.getBean(EphemerisController.class);
		Assert.assertNotNull(ephemerisController);
	}
}
