package com.maxar.weather.dm.manager;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.weather.repository.RTNEPHQuarterGridRepository;
import com.maxar.weather.repository.WTMRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testweatherdmnoingesters.properties")
public class WeatherDataManagerNoIngestersTest
{
	@Autowired
	private WeatherDataManagerTestImplementation weatherDataManager;

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

		weatherDataManager.launch(null);
	}

	@Test
	public void testStillCreated() {
		Assert.assertNotNull(weatherDataManager);
	}
}
