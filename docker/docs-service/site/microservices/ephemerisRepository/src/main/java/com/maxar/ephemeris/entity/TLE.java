package com.maxar.ephemeris.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.maxar.ephemeris.model.EphemerisModel;
import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.ephemeris.model.TLEModel;
import com.maxar.ephemeris.entity.utils.TLEUtils;
import com.radiantblue.analytics.mechanics.orbit.elementproviders.TLEElementProvider;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper=true)
public class TLE extends
		Ephemeris
{
	private static final long serialVersionUID = 1L;

	@Column(name = "description")
	protected String description;

	@Column(name = "tlelineone")
	protected String tleLineOne;

	@Column(name = "tlelinetwo")
	protected String tleLineTwo;

	public TLE() {
		type = EphemerisType.TLE;
	}

	public TLE(
			final TLE tle ) {
		this(
				tle.scn,
				tle.description,
				tle.tleLineOne,
				tle.tleLineTwo);
		type = EphemerisType.TLE;
	}

	public TLE(
			final int scn,
			final String description,
			final String tleLineOne,
			final String tleLineTwo ) {

		super(
				scn);
		this.description = description;
		this.tleLineOne = tleLineOne;
		this.tleLineTwo = tleLineTwo;
		type = EphemerisType.TLE;

		final TLEElementProvider tleProvider = new TLEElementProvider(
				description,
				tleLineOne,
				tleLineTwo);

		epochMillis = tleProvider.epoch().getMillis();

	}

	public TLE(
			final String description,
			final String tleLineOne,
			final String tleLineTwo ) {

		this(
				TLEUtils.getSCNFromTleLineOne(tleLineOne),
				description,
				tleLineOne,
				tleLineTwo);
		type = EphemerisType.TLE;
	}

	@Override
	public String toString() {
		return String.format(	"%s %s",
								super.toString(),
								description);
	}

	@Override
	public EphemerisModel toModel () {
		return TLEModel.tleBuilder()
				.scn(scn)
				.type(type)
				.epochMillis(epochMillis)
				.description(description)
				.tleLineOne(tleLineOne)
				.tleLineTwo(tleLineTwo)
				.build();
	}
}
