package com.maxar.ephemeris.dm;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.ephemeris.dm.manager.EphemerisDataManager;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:testephemerisdm.properties")
public class EphemerisDMApplicationTest
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		final EphemerisDataManager manager = applicationContext.getBean(EphemerisDataManager.class);

		Assert.assertNotNull(manager);
	}
}
