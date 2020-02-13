package com.maxar.spaceobjectcatalog.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maxar.ephemeris.entity.Ephemeris;

@Repository
public interface SpaceObjectCatalogRepository extends
		JpaRepository<Ephemeris, UUID>
{
	List<Ephemeris> findByScnOrderByEpochMillisDesc(
			final Integer scn,
			Pageable pageable );

	long countByScn(
			final Integer scn );

	List<Ephemeris> findByScnAndEpochMillisGreaterThanEqualAndEpochMillisLessThanEqualOrderByEpochMillisDesc(
			final Integer scn,
			final Long startMillis,
			final Long endMillis );
}
