package com.maxar.ephemeris.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxar.common.types.Vector3D;
import com.maxar.ephemeris.model.StateVectorModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@Table(name = "statevector", indexes = {
	@Index(name = "IDX_fk_statevector_ephemeriskey", columnList = "ephemeriskey")
})
public class StateVector implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "stateVectorykey", columnDefinition = "UUID")
	@JsonIgnore
	private UUID stateVectorykey;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ephemerisKey", referencedColumnName = "ephemerisKey",nullable=false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	private StateVectorSet stateVectorSet;
	
	@Column(name = "attimemillis")
	private long atTimeMillis;

	@Column(name = "ecfpos_x")
	private double ecfPosX;

	@Column(name = "ecfpos_y")
	private double ecfPosY;

	@Column(name = "ecfpos_z")
	private double ecfPosZ;

	@Column(name = "ecfvel_x")
	private double ecfVelX;

	@Column(name = "ecfvel_y")
	private double ecfVelY;

	@Column(name = "ecfvel_z")
	private double ecfVelZ;

	@Column(name = "ecfaccel_x")
	private double ecfAccelX;

	@Column(name = "ecfaccel_y")
	private double ecfAccelY;

	@Column(name = "ecfaccel_z")
	private double ecfAccelZ;

	@Column(name = "ecipos_x")
	private double eciPosX;

	@Column(name = "ecipos_y")
	private double eciPosY;

	@Column(name = "ecipos_z")
	private double eciPosZ;

	@Column(name = "ecivel_x")
	private double eciVelX;

	@Column(name = "ecivel_y")
	private double eciVelY;

	@Column(name = "ecivel_z")
	private double eciVelZ;

	@Column(name = "eciaccel_x")
	private double eciAccelX;

	@Column(name = "eciaccel_y")
	private double eciAccelY;

	@Column(name = "eciaccel_z")
	private double eciAccelZ;

	public StateVector(
			final long atTimeMillis,
			final Vector3D ecfPos,
			final Vector3D ecfVel,
			final Vector3D ecfAccel,
			final Vector3D eciPos,
			final Vector3D eciVel,
			final Vector3D eciAccel ) {
		this.atTimeMillis = atTimeMillis;
		ecfPosX = ecfPos.getX();
		ecfPosY = ecfPos.getY();
		ecfPosZ = ecfPos.getZ();
		ecfVelX = ecfVel.getX();
		ecfVelY = ecfVel.getY();
		ecfVelZ = ecfVel.getZ();
		ecfAccelX = ecfAccel.getX();
		ecfAccelY = ecfAccel.getY();
		ecfAccelZ = ecfAccel.getZ();
		eciPosX = eciPos.getX();
		eciPosY = eciPos.getY();
		eciPosZ = eciPos.getZ();
		eciVelX = eciVel.getX();
		eciVelY = eciVel.getY();
		eciVelZ = eciVel.getZ();
		eciAccelX = eciAccel.getX();
		eciAccelY = eciAccel.getY();
		eciAccelZ = eciAccel.getZ();
	}

	public StateVector(
			final StateVector sv ) {
		this(
				sv.atTimeMillis,
				new Vector3D(
						sv.ecfPosX,
						sv.ecfPosY,
						sv.ecfPosZ),
				new Vector3D(
						sv.ecfVelX,
						sv.ecfVelY,
						sv.ecfVelZ),
				new Vector3D(
						sv.ecfAccelX,
						sv.ecfAccelY,
						sv.ecfAccelZ),
				new Vector3D(
						sv.eciPosX,
						sv.eciPosY,
						sv.eciPosZ),
				new Vector3D(
						sv.eciVelX,
						sv.eciVelY,
						sv.eciVelZ),
				new Vector3D(
						sv.eciAccelX,
						sv.eciAccelY,
						sv.eciAccelZ));
	}

	public StateVector() {}

	@Override
	public String toString() {
		final String ret = String.format(	"ECF POS:(%f,%f,%f)\n",
											ecfPosX,
											ecfPosY,
											ecfPosZ)
				+ String.format("ECF VEL:(%f,%f,%f)\n",
								ecfVelX,
								ecfVelY,
								ecfVelZ)
				+ String.format("ECF ACCEL:(%f,%f,%f)\n",
								ecfAccelX,
								ecfAccelY,
								ecfAccelZ)
				+ String.format("ECI POS:(%f,%f,%f)\n",
								eciPosX,
								eciPosY,
								eciPosZ)
				+ String.format("ECI VEL:(%f,%f,%f)\n",
								eciVelX,
								eciVelY,
								eciVelZ)
				+ String.format("ECI ACCEL:(%f,%f,%f)\n",
								eciAccelX,
								eciAccelY,
								eciAccelZ);
		return ret;
	}

	public StateVectorModel toModel() {
		return StateVectorModel.stateVectorBuilder()
				.atTimeMillis(atTimeMillis)
				.ecfPos(new Vector3D(
						ecfPosX,
						ecfPosY,
						ecfPosZ))
				.ecfVel(new Vector3D(
						ecfVelX,
						ecfVelY,
						ecfVelZ))
				.ecfAccel(new Vector3D(
						ecfAccelX,
						ecfAccelY,
						ecfAccelZ))
				.eciPos(new Vector3D(
						eciPosX,
						eciPosY,
						eciPosZ))
				.eciVel(new Vector3D(
						eciVelX,
						eciVelY,
						eciVelZ))
				.eciAccel(new Vector3D(
						eciAccelX,
						eciAccelY,
						eciAccelZ))
				.build();
	}
}
