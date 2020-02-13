package com.maxar.weather.entity.map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxar.weather.model.map.WTMModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "wtm", indexes = {
	@Index(name = "IDX_wtmid", columnList = "wtmid"),
	@Index(name = "IDX_fk_atckey", columnList = "atckey")
})
@Data
@EqualsAndHashCode(callSuper=true)
public class WTM extends
		MapGrid
{
	private static final long serialVersionUID = 1L;

	@Column(name = "wtmid", nullable = false)
	private int wtmId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "atckey", referencedColumnName = "mapgridkey")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	private ATC atc;

	public WTM() {
		super();
	}

	public WTM(
			final ATC atc,
			final int wtmId ) {
		super(
				String
						.format(
								"%s%02d",
								atc.getId(),
								wtmId));
		this.atc = atc;
		this.wtmId = wtmId;
	}

	public WTMModel toModel() {
		return WTMModel.wtmBuilder()
				.wtmId(wtmId)
				.id(getId())
				.geometry(getGeometry())
				.build();
	}
}
