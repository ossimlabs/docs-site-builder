package com.maxar.opgen.model;

import org.joda.time.DateTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class OpSpaceRequest extends
		OpRequest
{
	/*
	 * Example Request 
	 * {
     * "assetName": "CSM3",
     * "sensorModeName": "CSM3_Framing_Mode",
     * "targetGeometryWkt": "POLYGON((117.23 33.94 0.0,117.25 33.94 0.0,117.25 33.92 0.0,117.23 33.92 0.0,117.23 33.94 0.0))",
     * "startTime": "2020-01-01T03:00:00.000Z",
     * "endTime": "2020-01-01T04:00:00.000Z",
     * "opSampleTime_ms": 10000,
     * "propagatorType": "J2"
     * }
	 */

	// Override the getters to get unique examples
	@Override
	@ApiModelProperty(position = 1, required = true, example = "CSM3", notes = "The name of the asset.")
	public String getAssetName() {
		return super.getAssetName();
	}

	@Override
	@ApiModelProperty(position = 2, required = true, example = "CSM3_Framing_Mode", notes = "The name of the sensor mode to use.")
	public String getSensorModeName() {
		return super.getSensorModeName();
	}

	@Override
	@ApiModelProperty(position = 3, required = true, example = "POLYGON((117.23 33.94 0.0,117.25 33.94 0.0,117.25 33.92 0.0,117.23 33.92 0.0,117.23 33.94 0.0))", notes = "The geometry of the target (in well-known text, WKT; "
			+ "points should be longitude (degrees), latitude (degrees), altitude (meters))")
	public String getTargetGeometryWkt() {
		return super.getTargetGeometryWkt();
	}

	@Override
	@ApiModelProperty(position = 4, required = true, example = "2020-01-01T03:00:00.000Z", notes = "The requested start time of the op or the start time range (ISO-8601)")
	public DateTime getStartTime() {
		return super.getStartTime();
	}

	@Override
	@ApiModelProperty(position = 5, required = false, example = "2020-01-01T04:00:00.000Z", notes = "The requested end time range (ISO-8601)")
	public DateTime getEndTime() {
		return super.getEndTime();
	}

	@Override
	@ApiModelProperty(position = 6, required = false, example = "10000", notes = "The op sampling time (in ms)")
	public int getOpSampleTime_ms() {
		return super.getOpSampleTime_ms();
	}

	// The propagator type to use (optional). If not specified will be chosen
	// depending on how old the ephemeris is.
	@Getter
	@Setter
	@ApiModelProperty(position = 7, required = false, example = "J2")
	private String propagatorType;
}
