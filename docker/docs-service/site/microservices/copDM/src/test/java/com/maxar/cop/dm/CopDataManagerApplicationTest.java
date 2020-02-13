package com.maxar.cop.dm;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.cop.dm.manager.CopDataManager;
import com.maxar.cop.dm.service.CopServiceClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:test-cop-dm-service.properties")
public class CopDataManagerApplicationTest
{
	@Autowired
	private ApplicationContext applicationContext;

	@MockBean
	private CopServiceClient serviceClient;

	@Before
	public void setUp() {
		Mockito.when(serviceClient.getSpaceAssetIDs())
				.thenReturn(Collections.emptyList());
	}

	@Test
	public void contextLoads() {
		final CopDataManager manager = applicationContext.getBean(CopDataManager.class);

		Assert.assertNotNull(manager);
	}
}
