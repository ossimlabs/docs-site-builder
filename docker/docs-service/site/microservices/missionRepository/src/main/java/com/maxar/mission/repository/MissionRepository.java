package com.maxar.mission.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maxar.mission.entity.Mission;

@Repository
public interface MissionRepository extends
		JpaRepository<Mission, UUID>
{
	Mission findById(
			final String id );

	long countByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(
			final String assetId,
			final Long start,
			final Long stop );

	List<Mission> findByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(
			final String assetId,
			final Long start,
			final Long stop );

	List<Mission> findByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(
			final String assetId,
			final Long start,
			final Long stop,
			final Pageable pageable );
}
