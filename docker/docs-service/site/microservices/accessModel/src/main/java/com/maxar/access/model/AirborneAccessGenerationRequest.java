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
public class AirborneAccessGenerationRequest
{
	
	private final static String adhocExample = "{\"onStationTime\": " + AdhocMission.exampleOnStation + "," + 
			"\"offStationTime\": " + AdhocMission.exampleOffStation + "," + 
			"\"waypoints\": " + AdhocMission.exampleWaypoints + "," + 
			"\"altitudeMeters\": " + AdhocMission.exampleAltitude + "}";
	
	@ApiModelProperty(position = 0, required = true, example = "POINT(28.759456 50.380964 135.0)", notes = "The geometry of the target (in well-known text, WKT; "
			+ "points should be longitude (radians), latitude (radians), altitude (meters)); "
			+ "alternatively a known target ID may be provided for lookup.")
	private String tgtOrGeometryWkt;

	@ApiModelProperty(position = 1, required = true, example = "2020-01-01T15:30:00.000Z", notes = "The start of the access generation time window")
	private String startTimeISO8601;

	@ApiModelProperty(position = 2, required = true, example = "2020-01-01T17:00:00.000Z", notes = "The end of the access generation time window")
	private String endTimeISO8601;

	@ApiModelProperty(position = 3, required = true, example = "[ { \"name\": \"Graze\", \"minValue\": 0, \"maxValue\": 90 }, "
			+ "{ \"name\": \"Quality\", \"minValue\": 0.0, \"maxValue\": null } ]", notes = "The list of access generation constraints")
	private List<AccessConstraint> accessConstraints;

	@ApiModelProperty(position = 4, required = false, example = "RADAR", notes = "The sensor type to generate access for")
	private String sensorType;

	@ApiModelProperty(position = 5, required = false, example = adhocExample, notes = "User-defined mission/track to generate access for; "
			+ "If not provided, mission(s) will be looked up by asset and timeframe.")
	private AdhocMission adhocMission;

	@ApiModelProperty(position = 6, required = false, example = "[AIR_SAR]", notes = "The asset name(s) to generate access for; "
			+ "If none are provided all available assets will be used.")
	private List<String> assetNames;
}
