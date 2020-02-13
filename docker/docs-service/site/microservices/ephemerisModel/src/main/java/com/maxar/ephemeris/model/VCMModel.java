package com.maxar.ephemeris.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class VCMModel extends
		EphemerisModel
{
	private String vcm;

	public VCMModel() {
		setType(EphemerisType.VCM);
	}

	@Builder(builderMethodName = "vcmBuilder")
	public VCMModel(
			final int scn,
			final EphemerisType type,
			final long epochMillis,
			final String vcm ) {
		super(
				scn,
				type,
				epochMillis);
		this.vcm = vcm;
	}

	public VCMModel(
			final VCMModel v ) {
		this(
				v.getScn(),
				v.getType(),
				v.getEpochMillis(),
				v.vcm);
		setType(EphemerisType.VCM);
	}

	@Override
	public String toString() {
		return String.format(	"%s %s",
								super.toString(),
								vcm);
	}
}
