package com.maxar.opgen.model.czml;

import lombok.Data;

@Data
public class OpCzmlProperties
{
	double width = 2.0;
	double outlineWidth = 0.0;
	String color = "FFFFFFFF"; // default color is white
	String eoColor = "FFFFFF00"; // default color for EO ops is yellow
	String radarColor = "FF0000FF"; // default color for radar ops is blue
	String irColor = "FFFF0000"; // default color for IR ops is red
	String outlineColor = "00000000";

	int displayDurationSec = 60000000;
}
