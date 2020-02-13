package com.maxar.geometric.intersection.repository;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.geometric.intersection.entity.AreaOfInterest;

@Repository
public interface AreaOfInterestRepository extends
		JpaRepository<AreaOfInterest, String>
{
	@Query(value = "SELECT a FROM AreaOfInterest a WHERE INTERSECTS(a.geometry, :geom) = TRUE")
	List<AreaOfInterest> findByIntersectingGeometry(
			@Param("geom")
			final Geometry geom);
}
