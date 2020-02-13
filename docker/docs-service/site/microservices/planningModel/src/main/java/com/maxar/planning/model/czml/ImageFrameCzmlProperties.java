package com.maxar.planning.model.czml;

import lombok.Data;

@Data
public class ImageFrameCzmlProperties
{
	double outlineWidth = 1.0;
	String outlineColor = "FFFFFFFF";
	boolean fill = false;
	boolean displayFrameInTree = false;
}
