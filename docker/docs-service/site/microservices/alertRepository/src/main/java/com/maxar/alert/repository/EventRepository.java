package com.maxar.alert.repository;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.alert.entity.Event;

@Repository
public interface EventRepository extends
		JpaRepository<Event, String>
{
	@Query(value = "Select t from Event t where intersects(t.geometry, :geom) = true")
	List<Event> findByGeometry(
			@Param("geom")
			final Geometry geom );
}
