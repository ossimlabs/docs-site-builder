package com.maxar.weather.repository;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.weather.entity.map.MapGrid;

@Repository
public interface MapGridRepository extends
		BaseMapGridRepository<MapGrid>
{
	@Query(value = "Select m from MapGrid m " + " where intersects(m.geometry, :geom) = true")
	public List<MapGrid> findByGeometry(
			@Param("geom" ) Geometry geom);
}
