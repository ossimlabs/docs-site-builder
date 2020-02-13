package com.maxar.asset.model.czml;

import lombok.Data;

@Data
public class AssetSmearCzmlProperties
{
	// smear
	final String smearAlpha = "1E"; // default to semi-transparent
	String smearOutlineColor = "FFFF0000";
	double smearOutlineWidth = 2.0;
	int smearLeadTimeSec = 10;
	int smearTrailTimeSec = 10;
	boolean displaySmearOnTimeline = true;
	boolean createSmearRootNode = false;

	// FOR Frames
	final String forAlpha = "FF"; // default to solid
	String forFrameOutlineColor = "FFFF0000";
	double forFrameOutlineWidth = 2.0;

	// asset
	private final int assetSamplingMillis = 1000;
	double assetTrailTimeSec = 150.0;
	private final double assetOutlineWidth = 4.0;

	// centroid/beam
	double centroidPixelSize = 4.0;
	String beamColor = "FFFFFFFF";
	int beamDurationSec = 30;
	double beamOutlineWidth = 2.0;
	boolean displayCentroidOnTimeline = true;

	// colors by sensor type
	// NOTE: These do NOT include the alpha!!!
	String color = "FFFFFF"; // default color is white
	String eoColor = "FFFF00"; // default color for EO ops is yellow
	String radarColor = "0000FF"; // default color for radar ops is blue
	String irColor = "FF0000"; // default color for IR ops is red
}
