package com.maxar.common.czml;

import lombok.Data;

@Data
public class VehiclePositionCzmlProperties
{
	double positionLinewidth = 2.0;
	double positionOutlineWidth = 2.0;
	String positionColor = "FFFFFFFF";
	String positionOutlineColor = "FFFFFFFF";
	double groundTraceLinewidth = 2.0;
	double groundTraceOutlineWidth = 2.0;
	String groundTraceColor = "FF00FF00";
	String groundTraceOutlineColor = "FF00FF00";
	double trailTime = 300.0;
}
