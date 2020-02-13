package com.maxar.ephemeris.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.ephemeris.entity.Ephemeris;

@Repository
public interface EphemerisRepository extends
		JpaRepository<Ephemeris, UUID>
{

	Ephemeris findFirstByScnOrderByEpochMillisDesc(
			Integer scn );

	public Ephemeris findFirstByScnAndEpochMillisLessThanEqualOrderByEpochMillisDesc(
			@Param("scn") Integer scn,
			@Param("atTime") Long atTime );
}
