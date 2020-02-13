package com.maxar.asset.common.aircraft;

import com.radiantblue.analytics.core.Vector3D;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PosVel
{
	public GeodeticPoint pos;
	public Vector3D vel;
}
