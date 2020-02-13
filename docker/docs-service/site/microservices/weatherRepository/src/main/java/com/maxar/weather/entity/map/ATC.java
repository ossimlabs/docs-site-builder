package com.maxar.weather.entity.map;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "atc", indexes = {
	@Index(name = "IDX_atcid", columnList = "atcid"),
	@Index(name = "IDX_fk_wackey", columnList = "wackey")
})
@Data
@EqualsAndHashCode(callSuper=true)
public class ATC extends
		MapGrid
{
	private static final long serialVersionUID = 1L;

	@Column(name = "atcid", nullable = false)
	private int atcId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wackey", referencedColumnName = "mapgridkey")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	private WAC wac;

	@OneToMany(mappedBy = "atc", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private Set<WTM> wtms = new HashSet<>();

	public ATC() {
		super();
	}

	public ATC(
			final WAC wac,
			final int atcId ) {
		super(
				String.format(
						"%s%02d",
						wac.getId(),
						atcId));
		this.wac = wac;
		this.atcId = atcId;
	}
}
