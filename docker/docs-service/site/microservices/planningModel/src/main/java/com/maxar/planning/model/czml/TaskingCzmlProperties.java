package com.maxar.planning.model.czml;

import lombok.Data;

@Data
public class TaskingCzmlProperties
{
	String color = "00000000";
	double pixelSize = 1.0;
	double outlineWidth = 1.0;
	String outlineColor = "00000000";
	int airborneTaskingDurationMillis = 1;
	boolean displayTaskingInTree = false;
}
