package com.maxar.access.model.czml;

import lombok.Data;

@Data
public class AccessCzmlProperties
{
	double width = 2.0;
	double outlineWidth = 0.0;
	String color = "FFFFFFFF"; // default color is white
	String eoColor = "FFFFFF00"; // default color for EO ops is yellow
	String radarColor = "FF0000FF"; // default color for radar ops is blue
	String irColor = "FFFF0000"; // default color for IR ops is red
	String outlineColor = "00000000";
	double staticWidth = 2.0;
	double staticOutlineWidth = 0.0;
	String staticColor = "FFFFFFFF"; // white
	String staticOutlineColor = "00000000";
	long samplingMS = 5000;
	boolean displayAccessInTree = false;
	boolean displayStaticAccessInTree = false;
	boolean displayAssetGroundTrace = false;
	long assetStartTimeBufferMs = 0; // Time (milliseconds) before TCA to show asset position
	long assetEndTimeBufferMs = 0; // Time (milliseconds) after TCA to show asset position
}
