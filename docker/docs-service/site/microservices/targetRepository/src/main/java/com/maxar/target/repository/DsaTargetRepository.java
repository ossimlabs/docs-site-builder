package com.maxar.target.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.maxar.target.entity.DsaTarget;

public interface DsaTargetRepository extends
		JpaRepository<DsaTarget, UUID>
{
	DsaTarget findByTargetId(
			String targetId );

	List<DsaTarget> findByCountryCode(
			String countryCode,
			Pageable pageable );

	long countByCountryCode(
			String countryCode );
}
