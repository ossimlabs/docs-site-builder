package com.maxar.target.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.maxar.target.entity.PointTarget;

public interface PointTargetRepository extends
		JpaRepository<PointTarget, UUID>
{
	PointTarget findByTargetId(
			String targetId );

	List<PointTarget> findByCountryCode(
			String countryCode,
			Pageable pageable );

	long countByCountryCode(
			String countryCode );
}
