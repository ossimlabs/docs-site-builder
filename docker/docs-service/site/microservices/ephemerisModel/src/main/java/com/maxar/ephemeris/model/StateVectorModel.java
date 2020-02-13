package com.maxar.ephemeris.model;

import com.maxar.common.types.Vector3D;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StateVectorModel
{
	private long atTimeMillis;
	private Vector3D ecfPos;
	private Vector3D ecfVel;
	private Vector3D ecfAccel;
	private Vector3D eciPos;
	private Vector3D eciVel;
	private Vector3D eciAccel;

	@Builder(builderMethodName = "stateVectorBuilder")
	public StateVectorModel(
			final long atTimeMillis,
			final Vector3D ecfPos,
			final Vector3D ecfVel,
			final Vector3D ecfAccel,
			final Vector3D eciPos,
			final Vector3D eciVel,
			final Vector3D eciAccel ) {
		this.atTimeMillis = atTimeMillis;
		this.ecfPos = ecfPos;
		this.ecfVel = ecfVel;
		this.ecfAccel = ecfAccel;
		this.eciPos = eciPos;
		this.eciVel = eciVel;
		this.eciAccel = eciAccel;
	}
}