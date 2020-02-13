package com.maxar.weather.model.weather;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Request object to get weather for geometry and date list.
 * Unequal list sizes will result in error
 *
 */
@Data
@ApiModel
public class WeatherByDateAndGeometryRequestBody
{
	final private static String exampleWKTString = "POLYGON((176.0 64.0,176.6 64.0,"
			+ "176.6 63.8,176.0 63.8,176.0 64.0))";
	
	@ApiModelProperty(position = 0, required = true, dataType = "List",
			example = "[{'dateTimeISO8601':'2020-01-01T05:00:00.000Z', 'geometryWKT':'POINT(-77.38368 38.96686 135.0)'}," +
						"{'dateTimeISO8601':'2020-01-01T07:00:00.000Z', 'geometryWKT':'" + exampleWKTString + "'}," + 
					"{'dateTimeISO8601':'2020-01-01T09:00:00.000Z', 'geometryWKT':'POINT(-77.38368 38.96686 135.0)'}]")
	List<WeatherByDateAndGeometryRequest> weatherRequestList;
	
	String parentId;
}
