package com.maxar.weather.model.weather.czml;

import lombok.Data;

@Data
public class WeatherByDateCzmlProperties
{
	private double outlineWidth = 2.0;
	private String outlineColor = "FF000000";
	private int unavailableTimeSeconds = 3600;
}
