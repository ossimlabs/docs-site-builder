package com.maxar.weather.model.weather;

import lombok.Data;

@Data
public class WeatherByDateAndGeometryRequest
{
	private String dateTimeISO8601;
	private String geometryWKT;
}
