package com.maxar.weather.entity.map;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "wac", indexes = {
	@Index(name = "IDX_wacid", columnList = "wacid", unique = true)
})
@Data
@EqualsAndHashCode(callSuper=true)
public class WAC extends
		MapGrid
{
	private static final long serialVersionUID = 1L;

	@Column(name = "wacid", nullable = false)
	private int wacId;

	@OneToMany(mappedBy = "wac", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private Set<ATC> atcs = new HashSet<>();

	public WAC() {
		super();
	}

	public WAC(
			final int wacId ) {
		super(
				String.format(
						"%04d",
						wacId));
		this.wacId = wacId;
	}
}
