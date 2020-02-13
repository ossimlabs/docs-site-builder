package com.maxar.target.repository;

import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.target.entity.Target;

@Repository
public interface TargetRepository extends
		JpaRepository<Target, UUID>
{
	Target findByTargetId(
			String targetId );

	List<Target> findByCountryCode(
			String countryCode,
			Pageable pageable );

	@Query(value = "Select t from Target t where intersects(t.geometry, :geom) = true")
	List<Target> findByGeometry(
			@Param("geom") Geometry geom );

	@Query(value = "Select t from Target t where (:geom is null or intersects(t.geometry, :geom) = true) and (coalesce(:ccList) is null or " +
			"t.countryCode in :ccList)")
	List<Target> findTargetByGeometryAndCountryCodeList(
			@Param("geom") Geometry geom,
			@Param("ccList") List<String> ccList );

	long countByCountryCode(
			String countryCode );
}
