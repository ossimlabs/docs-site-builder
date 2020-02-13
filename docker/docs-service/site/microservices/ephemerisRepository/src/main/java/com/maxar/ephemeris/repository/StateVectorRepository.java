package com.maxar.ephemeris.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maxar.ephemeris.entity.StateVector;

@Repository
public interface StateVectorRepository extends
		JpaRepository<StateVector, UUID>
{
}
