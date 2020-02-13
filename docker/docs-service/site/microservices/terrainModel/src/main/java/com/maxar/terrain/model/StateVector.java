package com.maxar.terrain.model;

import org.joda.time.DateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The state of an object.
 */
@ApiModel
@Data
@NoArgsConstructor
public class StateVector
{
	/** The time of this state vector. */
	@ApiModelProperty(position = 0,
					  required = true,
					  example = "2019-05-21T18:45:00.000Z",
					  notes = "The time for this state (ISO-8601)")
	private DateTime atTime;

	/** The latitude in degrees. */
	@ApiModelProperty(position = 1,
					  required = true,
					  example = "39.0",
					  notes = "The latitude for this state (in degrees)")
	private double latitude;

	/** The longitude in degrees. */
	@ApiModelProperty(position = 2,
					  required = true,
					  example = "-77.4",
					  notes = "The longitude for this state (in degrees)")
	private double longitude;

	/** The altitude in meters. */
	@ApiModelProperty(position = 3,
					  required = true,
					  example = "10000.0",
					  notes = "The altitude for this state (in meters)")
	private double altitude;
}
