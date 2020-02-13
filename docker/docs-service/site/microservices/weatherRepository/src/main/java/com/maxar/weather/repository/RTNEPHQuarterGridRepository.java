package com.maxar.weather.repository;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.weather.entity.map.RTNEPHQuarterGrid;

@Repository
public interface RTNEPHQuarterGridRepository extends
		BaseMapGridRepository<RTNEPHQuarterGrid>
{
	@Query(value = "Select r from RTNEPHQuarterGrid r " + " where intersects(r.geometry, :geom) = true")
	public List<RTNEPHQuarterGrid> findByGeometry(
			@Param("geom") Geometry geom );

	public List<RTNEPHQuarterGrid> findAllByOrderByNorthernHemisphereDescIdAsc();
}
