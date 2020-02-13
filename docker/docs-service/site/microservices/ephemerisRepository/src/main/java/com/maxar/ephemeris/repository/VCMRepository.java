package com.maxar.ephemeris.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.maxar.ephemeris.entity.VCM;

public interface VCMRepository extends
		JpaRepository<VCM, UUID>
{
	VCM findFirstByScnOrderByEpochMillisDesc(
			Integer scn );

	public VCM findFirstByScnAndEpochMillisLessThanEqualOrderByEpochMillisDesc(
			@Param("scn") Integer scn,
			@Param("atTime") Long atTime );
}
