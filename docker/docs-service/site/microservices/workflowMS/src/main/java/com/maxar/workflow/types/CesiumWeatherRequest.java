package com.maxar.workflow.types;

import com.maxar.weather.model.weather.WeatherByDateAndGeometryRequestBody;

public class CesiumWeatherRequest extends
		CesiumRequest
{
	public CesiumWeatherRequest(
			final String parent,
			final boolean generateParent,
			final boolean displayInTree,
			final String url,
			final WeatherByDateAndGeometryRequestBody body ) {
		this.parent = parent;
		this.generateParent = generateParent;
		this.displayInTree = displayInTree;
		this.url = url;
		this.body = body;
	}
}
