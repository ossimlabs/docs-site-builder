package com.maxar.weather.dm.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public class WeatherProperties
{
	private final Map<String, String> RTNEPHWeather = new HashMap<>();

	private final Map<String, String> AFWAWeather = new HashMap<>();

	public Map<String, String> getRTNEPHWeather() {
		return RTNEPHWeather;
	}

	public Map<String, String> getAFWAWeather() {
		return AFWAWeather;
	}
}
