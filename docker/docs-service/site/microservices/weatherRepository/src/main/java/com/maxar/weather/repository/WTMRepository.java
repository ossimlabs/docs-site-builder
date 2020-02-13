package com.maxar.weather.repository;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.weather.entity.map.WTM;

@Repository
public interface WTMRepository extends
		BaseMapGridRepository<WTM>
{
	@Query(value = "Select w from WTM w " + " where intersects(w.geometry, :geom) = true")
	public List<WTM> findByGeometry(
			@Param("geom") Geometry geom );

	public List<WTM> findAllByOrderByIdAsc();
}
