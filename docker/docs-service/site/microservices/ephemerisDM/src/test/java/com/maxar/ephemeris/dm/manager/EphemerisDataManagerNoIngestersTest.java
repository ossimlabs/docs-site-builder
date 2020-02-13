package com.maxar.ephemeris.dm.manager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testephemerisdmnoingesters.properties")
public class EphemerisDataManagerNoIngestersTest
{
	@Autowired
	private EphemerisDataManager ephemerisDataManager;

	@Before
	public void setUp() {
		ephemerisDataManager.launch(null);
	}

	@Test
	public void testStillCreated() {
		Assert.assertNotNull(ephemerisDataManager);
	}
}
