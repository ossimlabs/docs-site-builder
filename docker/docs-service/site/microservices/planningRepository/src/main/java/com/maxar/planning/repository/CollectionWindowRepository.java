package com.maxar.planning.repository;

import static com.maxar.planning.repository.QueryConstants.COUNT_COLLECTION_WINDOW_QUERY;
import static com.maxar.planning.repository.QueryConstants.FIND_COLLECTION_WINDOW_QUERY;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.planning.entity.image.CollectionWindow;

@Repository
public interface CollectionWindowRepository extends
		JpaRepository<CollectionWindow, UUID>
{
	@Query(value = FIND_COLLECTION_WINDOW_QUERY)
	List<CollectionWindow> findCollectionWindows(
			@Param("assetScn")
			final Integer assetScn,
			@Param("assetName")
			final String assetName,
			@Param("start")
			final Long start,
			@Param("stop")
			final Long stop,
			@Param("targetId")
			final String targetId );

	@Query(value = COUNT_COLLECTION_WINDOW_QUERY)
	long countCollectionWindows(
			@Param("assetScn")
			final Integer assetScn,
			@Param("assetName")
			final String assetName,
			@Param("start")
			final Long start,
			@Param("stop")
			final Long stop,
			@Param("targetId")
			final String targetId );

	@Query(value = FIND_COLLECTION_WINDOW_QUERY, countQuery = COUNT_COLLECTION_WINDOW_QUERY)
	List<CollectionWindow> findCollectionWindowsPaged(
			@Param("assetScn")
			final Integer assetScn,
			@Param("assetName")
			final String assetName,
			@Param("start")
			final Long start,
			@Param("stop")
			final Long stop,
			@Param("targetId")
			final String targetId,
			final Pageable pageable );
}
