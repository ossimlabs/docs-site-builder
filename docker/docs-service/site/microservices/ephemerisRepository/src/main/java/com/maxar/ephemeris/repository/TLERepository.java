package com.maxar.ephemeris.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.ephemeris.entity.TLE;

@Repository
public interface TLERepository extends
		JpaRepository<TLE, UUID>
{

	TLE findFirstByScnOrderByEpochMillisDesc(
			Integer scn );

	public TLE findFirstByScnAndEpochMillisLessThanEqualOrderByEpochMillisDesc(
			@Param("scn") Integer scn,
			@Param("atTime") Long atTime );
}
