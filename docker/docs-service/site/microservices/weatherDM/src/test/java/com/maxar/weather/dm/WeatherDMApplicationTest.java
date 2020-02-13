package com.maxar.weather.dm;

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

import com.maxar.weather.dm.manager.WeatherDataManagerTestImplementation;
import com.maxar.weather.repository.RTNEPHQuarterGridRepository;
import com.maxar.weather.repository.WTMRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:testweatherdm.properties")
public class WeatherDMApplicationTest
{
	@Autowired
	private ApplicationContext applicationContext;

	@MockBean
	private RTNEPHQuarterGridRepository rtnephQuarterGridRepository;

	@MockBean
	private WTMRepository wtmRepository;

	@Before
	public void setUp() {
		Mockito.when(rtnephQuarterGridRepository.findAllByOrderByNorthernHemisphereDescIdAsc())
				.thenReturn(Collections.emptyList());

		Mockito.when(wtmRepository.findAllByOrderByIdAsc())
				.thenReturn(Collections.emptyList());
	}

	@Test
	public void contextLoads() {
		final WeatherDataManagerTestImplementation weatherDataManager = applicationContext
				.getBean(WeatherDataManagerTestImplementation.class);

		Assert.assertNotNull(weatherDataManager);
	}
}
