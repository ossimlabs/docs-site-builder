package com.maxar.weather.repository;

import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.weather.entity.weather.Weather;

@Repository
public interface WeatherRepository extends
		JpaRepository<Weather, UUID>
{
	@Query(value = "Select w from Weather w " + " inner join w.weatherSet ws on ws.weatherSetKey=:weatherSetKey"
			+ " inner join w.mapGrid m " + " where intersects(m.geometry, :geom) = true")
	public List<Weather> getByWeatherSetAndGeometry(
			@Param("weatherSetKey" ) UUID weatherSetKey,
			@Param("geom") Geometry geom);
}
