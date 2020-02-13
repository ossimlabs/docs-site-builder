package com.maxar.ephemeris.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxar.ephemeris.model.EphemerisModel;
import com.maxar.ephemeris.model.EphemerisType;
import com.radiantblue.analytics.core.DateTimeFactory;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
public class Ephemeris implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Column(name = "scn")
	protected int scn;

	@Column(name = "type")
	@Enumerated
	protected EphemerisType type;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "ephemeriskey", columnDefinition = "UUID")
	@JsonIgnore
	private UUID ephemerisKey;

	// Adding fields for database subclass...have to do this so TLE and
	// StateVectorSet have access
	@Column(name = "epochmillis")
	protected long epochMillis;
	
	private Integer priority;

	public Ephemeris(
			final int scn ) {
		this.scn = scn;
	}

	@Override
	public String toString() {
		return String.format(	"%05d %s %s",
								scn,
								DateTimeFactory.fromMillis(epochMillis)
										.toString(),
								type.toString());
	}
	
	public EphemerisModel toModel () {
		return EphemerisModel.builder()
				.scn(scn)
				.type(type)
				.epochMillis(epochMillis)
				.build();
	}
}
