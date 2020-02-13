package com.maxar.planning.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maxar.planning.entity.link.Link;

@Repository
public interface LinkRepository extends
		JpaRepository<Link, UUID>
{
	public List<Link> findByTargetId(
			String targetId );

	public List<Link> findByCrId(
			String crId );
}
