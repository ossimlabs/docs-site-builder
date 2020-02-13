package com.maxar.mission;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.mission.controller.MissionController;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource("classpath:testmissionms.properties")
public class MissionMsApplicationTests
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		final MissionController missionController = applicationContext.getBean(MissionController.class);

		Assert.assertNotNull(missionController);
	}
}
