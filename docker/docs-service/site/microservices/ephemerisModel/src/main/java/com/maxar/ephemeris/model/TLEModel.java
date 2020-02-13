package com.maxar.ephemeris.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TLEModel extends
		EphemerisModel
{
	private String description;
	private String tleLineOne;
	private String tleLineTwo;
	
	public TLEModel() {
		setType(EphemerisType.TLE);
	}
	
	@Builder(builderMethodName = "tleBuilder")
	public TLEModel(
			int scn,
			EphemerisType type,
			long epochMillis,
			String description,
			String tleLineOne,
			String tleLineTwo ) {
		super(
				scn,
				type,
				epochMillis);
		this.description = description;
		this.tleLineOne = tleLineOne;
		this.tleLineTwo = tleLineTwo;
	}
}
