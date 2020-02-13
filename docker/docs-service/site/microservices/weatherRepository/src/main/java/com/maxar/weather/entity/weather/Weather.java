package com.maxar.weather.entity.weather;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.maxar.weather.entity.map.MapGrid;

import lombok.Data;

@Entity
@Table(name = "weather", indexes = {
	@Index(name = "IDX_fk_weathersetkey", columnList = "weathersetkey")
})
@Data
public class Weather implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "weatherkey", columnDefinition = "UUID")
	private UUID weatherKey;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mapgridkey", referencedColumnName = "mapgridkey")
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	private MapGrid mapGrid;

	@Column(name = "cloudcoverpercent")
	private Double cloudCoverPercent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "weathersetkey")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private WeatherSet weatherSet;

	public Weather() {}

	public Weather(
			final WeatherSet weatherSet,
			final MapGrid mapGrid,
			final double cloudCoverPercent ) {
		this.weatherSet = weatherSet;
		this.mapGrid = mapGrid;
		this.cloudCoverPercent = cloudCoverPercent;
	}
}
