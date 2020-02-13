package com.maxar.common.types;

public class Vector3D
{
	private double x;
	private double y;
	private double z;

	public Vector3D() {}

	public Vector3D(
			final double x,
			final double y,
			final double z ) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public void setX(
			final double x ) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(
			final double y ) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(
			final double z ) {
		this.z = z;
	}

	public com.radiantblue.analytics.core.Vector3D toRBAnalyticsVector3D() {
		return new com.radiantblue.analytics.core.Vector3D(
				x,
				y,
				z);
	}
}
