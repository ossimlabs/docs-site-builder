package com.maxar.terrain.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * The response object for the endpoint to get good time intervals.
 */
@ApiModel
@Data
public class GoodTimeIntervalsResponse
{
	/**
	 * The list of time intervals when the target can be accessed.
	 */
	@ApiModelProperty(position = 0,
					  required = true,
					  example = "[ { \"start\": \"2019-05-21T18:45:00.000Z\", "
							  + "\"end\": \"2019-05-21T18:45:02.000Z\" }, "
							  + "{ \"start\": \"2019-05-21T18:45:06.000Z\", "
							  + "\"end\": \"2019-05-21T18:45:10.000Z\" } ]",
					  notes = "The list of time intervals when the target can be accessed")
	private List<TimeInterval> timeIntervals;
}
