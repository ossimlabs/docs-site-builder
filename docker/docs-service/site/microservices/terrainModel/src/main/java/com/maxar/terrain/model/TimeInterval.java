package com.maxar.terrain.model;

import org.joda.time.DateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A simplified time interval with a Joda start and end time.
 */
@ApiModel
@Data
@NoArgsConstructor
public class TimeInterval
{
	/** The start time of the interval. */
	@ApiModelProperty(position = 0,
					  required = true,
					  example = "2019-05-21T18:45:00.000Z",
					  notes = "The start time of the interval (ISO-8601)")
	private DateTime start;

	/** The end time of the interval. */
	@ApiModelProperty(position = 1,
					  required = true,
					  example = "2019-05-21T18:45:10.000Z",
					  notes = "The end time of the interval (ISO-8601)")
	private DateTime end;
}
