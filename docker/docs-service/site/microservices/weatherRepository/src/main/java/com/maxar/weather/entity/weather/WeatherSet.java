package com.maxar.weather.entity.weather;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.joda.time.DateTime;

@Entity
@Table(name = "weatherset", indexes = {
	@Index(name = "IDX_attimemillis", columnList = "attimemillis")
})
public class WeatherSet implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "weathersetkey", columnDefinition = "UUID")
	private UUID weatherSetKey;

	@Column(name = "attimemillis")
	private Long atTimeMillis;

	@OneToMany(mappedBy = "weatherSet", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Weather> weather = new HashSet<>();

	public WeatherSet() {}

	public WeatherSet(
			final DateTime atTime,
			final Set<Weather> weather ) {
		atTimeMillis = atTime.getMillis();
		this.weather = weather;
	}

	public UUID getWeatherSetKey() {
		return weatherSetKey;
	}

	public void setWeatherSetKey(
			final UUID weatherSetKey ) {
		this.weatherSetKey = weatherSetKey;
	}

	public Long getAtTimeMillis() {
		return atTimeMillis;
	}

	public void setAtTimeMillis(
			final Long atTimeMillis ) {
		this.atTimeMillis = atTimeMillis;
	}

	public Set<Weather> getWeather() {
		return weather;
	}

	public void setWeather(
			final Set<Weather> weather ) {
		this.weather = weather;
	}
}
