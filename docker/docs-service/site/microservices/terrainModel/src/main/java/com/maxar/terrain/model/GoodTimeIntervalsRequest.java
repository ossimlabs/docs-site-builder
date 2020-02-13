package com.maxar.terrain.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * The request object for the endpoint to get good time intervals.
 */
@ApiModel
@Data
public class GoodTimeIntervalsRequest
{
	/**
	 * The well-known text (WKT) geometry string. The center point of this will
	 * be used for the target's terrain mask generation. Points should be
	 * specified with the order longitude (degrees), latitude (degrees),
	 * altitude (meters).
	 */
	@ApiModelProperty(position = 0,
					  required = true,
					  example = "POINT(-77.3861 38.9696 135.0)",
					  notes = "The geometry of the target (in well-known text, WKT; "
							  + "points should be longitude (degrees), latitude (degrees), altitude (meters))")
	private String geometryWkt;

	/**
	 * The state vectors of the observer. The terrain service will use these to
	 * check the intervals where the terrain mask allows the target to be
	 * accessed.
	 */
	@ApiModelProperty(position = 1,
					  required = true,
					  example = "[ { \"atTime\": \"2019-05-21T18:45:00.000Z\", \"latitude\": 39, "
							  + "\"longitude\": -77.4, \"altitude\": 10000 }, "
							  + "{ \"atTime\": \"2019-05-21T18:45:02.000Z\", \"latitude\": 38.99, "
							  + "\"longitude\": -77.42, \"altitude\": 9000 }, "
							  + "{ \"atTime\": \"2019-05-21T18:45:04.000Z\", \"latitude\": 38.98, "
							  + "\"longitude\": -77.45, \"altitude\": 5 }, "
							  + "{ \"atTime\": \"2019-05-21T18:45:06.000Z\", \"latitude\": 38.97, "
							  + "\"longitude\": -77.47, \"altitude\": 8500 }, "
							  + "{ \"atTime\": \"2019-05-21T18:45:08.000Z\", \"latitude\": 38.96, "
							  + "\"longitude\": -77.50, \"altitude\": 9500 }, "
							  + "{ \"atTime\": \"2019-05-21T18:45:10.000Z\", \"latitude\": 38.95, "
							  + "\"longitude\": -77.52, \"altitude\": 10030 } ]",
					  notes = "The list of state vectors for the observer")
	private List<StateVector> stateVectors;
}
