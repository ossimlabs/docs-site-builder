package com.maxar.target.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.maxar.target.entity.LocTarget;

public interface LocTargetRepository extends
		JpaRepository<LocTarget, UUID>
{
	LocTarget findByTargetId(
			String targetId );

	List<LocTarget> findByCountryCode(
			String countryCode,
			Pageable pageable );

	long countByCountryCode(
			String countryCode );
}
