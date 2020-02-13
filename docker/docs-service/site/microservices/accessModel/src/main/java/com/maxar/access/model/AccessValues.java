package com.maxar.access.model;

import com.maxar.common.types.Vector3D;

import lombok.Data;

@Data
public class AccessValues
{
	private String atTimeISO8601;
	private double quality;
	private double gsdInches;
	private double grazeDeg;
	private double azimuthDeg;
	private double elevationDeg;
	private double squintDeg;
	private double sunAzimuthDeg;
	private double sunElevationDeg;
	private double specularReflectionDeg;
	private double moonAzimuthDeg;
	private double moonElevationDeg;
	private double moonIlluminationPct;
	private double nadirDeg;
	private double dopplerConeDeg;
	private double catsAngleDeg;
	private double slantRangeMeters;
	private double slantRangeKms;
	private double cloudCoverPct;
	private Vector3D assetPositionECF;
	private Vector3D targetPositionECF;
}
