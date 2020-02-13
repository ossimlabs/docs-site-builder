package com.maxar.ephemeris.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.ephemeris.entity.StateVectorSet;

@Repository
public interface StateVectorSetRepository extends
		JpaRepository<StateVectorSet, UUID>
{
	StateVectorSet findFirstByScnOrderByEpochMillisDesc(
			Integer scn );

	public StateVectorSet findFirstByScnAndEpochMillisLessThanEqualOrderByEpochMillisDesc(
			@Param("scn") Integer scn,
			@Param("atTime") Long atTime );

}
