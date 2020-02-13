package com.maxar.access.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * The request object for the endpoint to get accesses.
 */
@Data
@ApiModel
public class AccessGenerationRequest
{
	@ApiModelProperty(position = 0, required = true, example = "POINT(-77.38368 38.96686 135.0)", notes = "The geometry of the target (in well-known text, WKT; "
			+ "points should be longitude (radians), latitude (radians), altitude (meters)); "
			+ "alternatively a known target ID may be provided for lookup.")
	private String tgtOrGeometryWkt;

	@ApiModelProperty(position = 1, required = true, example = "2020-01-01T09:30:00.000Z", notes = "The start of the access generation time window")
	private String startTimeISO8601;

	@ApiModelProperty(position = 2, required = true, example = "2020-01-02T09:30:00.000Z", notes = "The end of the access generation time window")
	private String endTimeISO8601;

	@ApiModelProperty(position = 3, required = true, example = "[ { \"name\": \"Graze\", \"minValue\": 20, \"maxValue\": 70 }, "
			+ "{ \"name\": \"Quality\", \"minValue\": 4.0, \"maxValue\": null } ]", notes = "The list of access generation constraints")
	private List<AccessConstraint> accessConstraints;

	@ApiModelProperty(position = 4, example = "RADAR", notes = "The sensor type to generate access for")
	private String sensorType;

	@ApiModelProperty(position = 5, example = "SGP4", notes = "The propagator type for asset propagation")
	private String propagatorType;

	@ApiModelProperty(position = 6, example = "[RS02]", notes = "The asset ID(s) to generate access for; "
			+ "If none are provided all available assets will be used.")
	private List<String> assetIDs;

	@ApiModelProperty(position = 7, notes = "Start time buffer (milliseconds) for asset position data generated for CZML")
	private Long assetStartTimeBufferMs;

	@ApiModelProperty(position = 8, notes = "End time buffer (milliseconds) for asset position data generated for CZML")
	private Long assetEndTimeBufferMs;
}
