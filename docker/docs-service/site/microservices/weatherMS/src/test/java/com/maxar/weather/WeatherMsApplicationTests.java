package com.maxar.weather;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.weather.controller.WeatherController;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class WeatherMsApplicationTests
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		final WeatherController weatherController = applicationContext.getBean(WeatherController.class);

		Assert.assertNotNull(weatherController);
	}
}
