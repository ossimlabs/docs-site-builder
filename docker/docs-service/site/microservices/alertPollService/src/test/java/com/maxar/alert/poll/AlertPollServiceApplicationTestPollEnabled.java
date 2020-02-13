package com.maxar.alert.poll;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.alert.poll.service.AlertPollService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-alert-poll-enabled.properties")
public class AlertPollServiceApplicationTestPollEnabled
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		final AlertPollService alertPollService = applicationContext.getBean(AlertPollService.class);

		Assert.assertNotNull(alertPollService);
	}
}
