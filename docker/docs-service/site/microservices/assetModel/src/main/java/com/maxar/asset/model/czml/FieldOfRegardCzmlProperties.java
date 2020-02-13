package com.maxar.asset.model.czml;

import lombok.Data;

@Data
public class FieldOfRegardCzmlProperties
{
	double outlineWidth = 1.0;
	String outlineColor = "00000000";
	// colors by sensor type
	String color = "1EFFFFFF"; // default color is white
	String eoColor = "1EFFFF00"; // default color for EO ops is yellow
	String radarColor = "1E0000FF"; // default color for radar ops is blue
	String irColor = "1EFF0000"; // default color for IR ops is red
	int forDurationMillis = 1;
}
