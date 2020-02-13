package com.maxar.weather.entity.map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.locationtech.jts.geom.Geometry;

import com.maxar.weather.model.map.RTNEPHModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "rtnephquartergrid")
@Data
@EqualsAndHashCode(callSuper=true)
public class RTNEPHQuarterGrid extends
		MapGrid
{
	private static final long serialVersionUID = 1L;

	@Column(name = "rtnephid")
	private int rtnephId;

	@Column(name = "northernhemisphere")
	private boolean northernHemisphere;

	public RTNEPHQuarterGrid() {
		super();
	}

	public RTNEPHQuarterGrid(
			final boolean northernHemisphere,
			final int rtnephId,
			final Geometry geom ) {
		super(
				Integer
						.toString(
								rtnephId),
				geom);
		this.rtnephId = rtnephId;
		this.northernHemisphere = northernHemisphere;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public RTNEPHModel toModel() {
		return RTNEPHModel.rtnephBuilder()
				.rtnephId(rtnephId)
				.northernHemisphere(northernHemisphere)
				.id(getId())
				.geometry(getGeometry())
				.build();
	}
}
