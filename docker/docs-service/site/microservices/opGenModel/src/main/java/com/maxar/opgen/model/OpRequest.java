package com.maxar.opgen.model;

import org.joda.time.DateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OpRequest
{
	// the name of the asset to use
	String assetName;

	// the name of the sensor mode to use
	String sensorModeName;

	// The WKT geometry of the target.
	private String targetGeometryWkt;

	// The requested start time of the op, or the start of the time range if endTime
	// is also specified
	private DateTime startTime;

	// The requested time range end time
	private DateTime endTime;

	// The sampling time if a time range is specified (ignored if supplied with only
	// a start time)
	private int opSampleTime_ms;

	// TODO: Constraints?

}
