package com.maxar.planning.repository;

import static com.maxar.planning.repository.QueryConstants.COUNT_TASKING_QUERY;
import static com.maxar.planning.repository.QueryConstants.FIND_TASKING_QUERY;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.planning.entity.tasking.Tasking;

@Repository
public interface TaskingRepository extends
		JpaRepository<Tasking, UUID>
{
	@Query(value = FIND_TASKING_QUERY)
	List<Tasking> findTasking(
			@Param("missionId")
			final String missionId,
			@Param("start")
			final Long start,
			@Param("stop")
			final Long stop,
			@Param("targetId")
			final String targetId );

	@Query(value = COUNT_TASKING_QUERY)
	long countTasking(
			@Param("missionId")
			final String missionId,
			@Param("start")
			final Long start,
			@Param("stop")
			final Long stop,
			@Param("targetId")
			final String targetId );

	@Query(value = FIND_TASKING_QUERY, countQuery = COUNT_TASKING_QUERY)
	List<Tasking> findTaskingPaged(
			@Param("missionId")
			final String missionId,
			@Param("start")
			final Long start,
			@Param("stop")
			final Long stop,
			@Param("targetId")
			final String targetId,
			final Pageable pageable );
}
