package com.maxar.target.model.czml;

import lombok.Data;

@Data
public class TargetCzmlProperties
{
	double width = 2.0;
	double outlineWidth = 0.0;
	String color = "FFFFFFFF";
	String outlineColor = "00000000";
	
	int czmlTrailTimeSec = 10;
}
