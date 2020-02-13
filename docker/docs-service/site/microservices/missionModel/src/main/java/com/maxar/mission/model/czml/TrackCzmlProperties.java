package com.maxar.mission.model.czml;

import lombok.Data;

@Data
public class TrackCzmlProperties
{
	double trackWidth = 2.0;
	double trackOutlineWidth = 0.0;
	String trackColor = "FFFFFFFF";
	String trackOutlineColor = "00000000";
	
	double trackTraceWidth = 2.0;
	double trackTraceOutlineWidth = 0.0;
	String trackTraceColor = "FFFF0000";
	String trackTraceOutlineColor = "00000000";
	
	double trackNodeTrailTimeSec = 3600.0;
}
