package com.maxar.weather.dm.manager;

import org.springframework.stereotype.Controller;

@Controller
public class WeatherDataManagerTestImplementation extends
		WeatherDataManager
{
	@Override
	public void runCleanup() {
		super.runCleanup();
	}
}
