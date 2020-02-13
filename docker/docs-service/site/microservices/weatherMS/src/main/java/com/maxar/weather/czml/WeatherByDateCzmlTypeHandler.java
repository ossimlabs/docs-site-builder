package com.maxar.weather.czml;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.weather.model.weather.WeatherByDate;
import com.maxar.weather.model.weather.czml.WeatherByDateCzmlProperties;

public class WeatherByDateCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	WeatherByDateCzmlProperties properties;

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return WeatherByDate.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return false;
	}

	@Override
	public List<String> handle(
			final Object object ) {
		return Collections.singletonList(((WeatherByDate) object).produceCzml(properties, null));
	}
}
