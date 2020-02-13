package com.maxar.target.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.maxar.target.entity.BasTarget;

public interface BasTargetRepository extends
		JpaRepository<BasTarget, UUID>
{
	BasTarget findByTargetId(
			String targetId );

	List<BasTarget> findByCountryCode(
			String countryCode,
			Pageable pageable );

	long countByCountryCode(
			String countryCode );
}
