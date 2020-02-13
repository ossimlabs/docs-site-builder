package com.maxar.planning.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maxar.planning.entity.image.ImageOp;

@Repository
public interface ImageOpRepository extends
		JpaRepository<ImageOp, UUID>
{
	public List<ImageOp> findByOpId(
			String opId );

	public List<ImageOp> findByOpStartTimeMillisBetweenOrOpEndTimeMillisBetween(
			Long startStart,
			Long startEnd,
			Long endStart,
			Long endEnd );

	public List<ImageOp> findByLink_TargetId(
			String targetIdString );
}
