package com.maxar.target.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxar.target.model.BasWtmModel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "baswtm")
@Data
@NoArgsConstructor
public class BasWtm implements
		Comparable<BasWtm>
{
	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "baswtmid", columnDefinition = "UUID")
	@JsonIgnore
	private UUID basWtmId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parentbastargetkey", referencedColumnName = "targetkey")
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	protected BasTarget parentBas;

	@Column(name = "mapgridid")
	protected String mapGridId;

	public BasWtm(
			final BasTarget parentBas,
			final String mapGridId ) {
		this.parentBas = parentBas;
		this.mapGridId = mapGridId;
	}

	@Override
	public int compareTo(
			final BasWtm bw ) {
		final int myId = Integer.parseInt(mapGridId);
		final int bwId = Integer.parseInt(bw.mapGridId);
		return myId - bwId;
	}

	public BasWtmModel toModel() {
		return new BasWtmModel(mapGridId);
	}
}
