package com.maxar.ephemeris.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.maxar.ephemeris.model.EphemerisModel;
import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.ephemeris.model.VCMModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class VCM extends
		Ephemeris
{
	private static final long serialVersionUID = 1L;

	@Column(name = "vcm", columnDefinition="TEXT")
	protected String vcm;
	
	public VCM() {
		type = EphemerisType.VCM;
	}
	
	public VCM(final String vcm) {
		this.vcm = vcm;
		type = EphemerisType.VCM;
		
	}

	public VCM(
			final VCM vcm ) {
		this(
				vcm.scn,
				vcm.epochMillis,
				vcm.vcm);
		type = EphemerisType.VCM;
	}

	public VCM(
			final int scn,
			final long epochMillis,
			final String vcm ) {

		super(
				scn);
		this.epochMillis = epochMillis;
		this.vcm = vcm;
		type = EphemerisType.VCM;
	}

	@Override
	public String toString() {
		return String.format(	"%s %s",
								super.toString(),
								vcm);
	}

	@Override
	public EphemerisModel toModel() {
		return VCMModel.vcmBuilder()
				.scn(scn)
				.type(type)
				.epochMillis(epochMillis)
				.vcm(vcm)
				.build();
	}
}
