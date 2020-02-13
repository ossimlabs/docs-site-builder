package com.maxar.weather.dm.dataingest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

import com.maxar.weather.parser.AfwaWeatherParser;
import com.maxar.weather.entity.weather.WeatherSet;
import com.maxar.weather.repository.WeatherSetRepository;

public class WeatherFileIngesterTestAfwa
{
	private static final String WEATHER_PROPERTIES_FILE = "testweatherdm.properties";

	private static final String WEATHER_XML_BEANS_FILE = "testweatherafwa.xml";

	// This isn't a real weather data file, but that's okay, the parser is just
	// being mocked.
	private static final String WEATHER_DATA_FILE = "weather.xml";

	private GenericXmlApplicationContext genericXmlApplicationContext;

	private WeatherFileIngester weatherFileIngester;

	private File file;

	private WeatherSetRepository weatherSetRepository;

	private AfwaWeatherParser parser;

	@Before
	public void setUp()
			throws IOException,
			URISyntaxException {
		genericXmlApplicationContext = new GenericXmlApplicationContext();
		genericXmlApplicationContext.load(WEATHER_XML_BEANS_FILE);

		final Properties properties = new Properties();
		final InputStream propertiesStream = Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(WEATHER_PROPERTIES_FILE);

		Assert.assertNotNull(propertiesStream);

		properties.load(propertiesStream);

		final PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource(
				WEATHER_PROPERTIES_FILE,
				properties);

		genericXmlApplicationContext.getEnvironment()
				.getPropertySources()
				.addFirst(propertiesPropertySource);
		genericXmlApplicationContext.refresh();

		weatherFileIngester = genericXmlApplicationContext.getBean(WeatherFileIngester.class);

		final URL weatherDataUrl = Thread.currentThread()
				.getContextClassLoader()
				.getResource(WEATHER_DATA_FILE);

		Assert.assertNotNull(weatherDataUrl);

		file = new File(
				weatherDataUrl.toURI());

		weatherSetRepository = Mockito.mock(WeatherSetRepository.class);
		parser = Mockito.mock(AfwaWeatherParser.class);
	}

	@After
	public void tearDown() {
		genericXmlApplicationContext.close();
	}

	@Test
	public void testBeanLoaded() {
		Assert.assertNotNull(weatherFileIngester);
	}

	@Test
	public void testIngestToDatabaseWeatherSetEmpty()
			throws Exception {
		Mockito.when(parser.parseData(	Mockito.any(),
										Mockito.anyString()))
				.thenReturn(Collections.emptyList());

		Mockito.when(weatherSetRepository.deleteByAtTimeMillis(Mockito.anyLong()))
				.thenThrow(new RuntimeException(
						"should not have done anything with the repository"));

		Mockito.when(weatherSetRepository.saveAndFlush(Mockito.any()))
				.thenThrow(new RuntimeException(
						"should not have done anything with the repository"));

		weatherFileIngester.setWeatherSetRepository(weatherSetRepository);
		weatherFileIngester.setParser(parser);

		weatherFileIngester.ingestToDatabase(	file,
												null);

		Assert.assertTrue(true);
	}

	@Test
	public void testIngestToDatabaseWeatherSetOne()
			throws Exception {
		final WeatherSet weatherSet = new WeatherSet();
		weatherSet.setAtTimeMillis(0L);

		Mockito.when(parser.parseData(	Mockito.any(),
										Mockito.anyString()))
				.thenReturn(Collections.singletonList(weatherSet));

		Mockito.when(weatherSetRepository.deleteByAtTimeMillis(Mockito.eq(0L)))
				.thenReturn(0L);

		Mockito.when(weatherSetRepository.saveAndFlush(Mockito.any()))
				.thenReturn(null);

		weatherFileIngester.setWeatherSetRepository(weatherSetRepository);
		weatherFileIngester.setParser(parser);

		weatherFileIngester.ingestToDatabase(	file,
												null);

		Assert.assertTrue(true);
	}
}
