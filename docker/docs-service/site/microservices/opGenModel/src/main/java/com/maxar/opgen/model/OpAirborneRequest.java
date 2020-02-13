package com.maxar.opgen.model;

import org.joda.time.DateTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class OpAirborneRequest extends
		OpRequest
{
	/*
	 * Example Request { "assetName": "AIR_SAR", "sensorModeName":
	 * "AIR_SAR_Framing_Mode", "targetGeometryWkt":
	 * "POLYGON((29.273071289062496 26.56887654795065 0.0, 28.685302734375 26.140645014531376 0.0, 29.091796875 25.562265014427492 0.0, 29.739990234374996 25.82956108605351 0.0, 29.273071289062496 26.56887654795065 0.0))"
	 * , "startTime": "2015-01-01T13:00:00Z", "missionId": "Mission1" }
	 */

	// Override the getters to get unique examples
	@Override
	@ApiModelProperty(position = 1, required = true, example = "AIR_SAR", notes = "The name of the asset.")
	public String getAssetName() {
		return super.getAssetName();
	}

	@Override
	@ApiModelProperty(position = 2, required = true, example = "AIR_SAR_Framing_Mode", notes = "The name of the sensor mode to use.")
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
	@ApiModelProperty(position = 4, required = true, example = "2020-01-01T07:25:00Z", notes = "The requested start time of the op (ISO-8601)")
	public DateTime getStartTime() {
		return super.getStartTime();
	}

	@Override
	@ApiModelProperty(position = 5, required = false, example = "2020-01-01T08:25:00.000Z", notes = "The requested end time range (ISO-8601)")
	public DateTime getEndTime() {
		return super.getEndTime();
	}

	@Override
	@ApiModelProperty(position = 6, required = false, example = "60000", notes = "The op sampling time (in ms)")
	public int getOpSampleTime_ms() {
		return super.getOpSampleTime_ms();
	}

	// The missionId to use. If not specified will look up all missions for the
	// asset over the time range.
	@Getter
	@Setter
	@ApiModelProperty(position = 7, required = false, example = "MISSION_12")
	private String missionId;

}
