package com.maxar.planning.repository;

final class QueryConstants
{
	private static final String COMMON_COLLECTION_WINDOW_QUERY = "from CollectionWindow c "
			+ "join c.imageOps io join io.link l where (:assetScn is null or c.assetScn = :assetScn) "
			+ "and (:assetName is null or c.assetName = :assetName) and (:start is null or c.endMillis >= :start) "
			+ "and (:stop is null or c.startMillis <= :stop) and (:targetId is null or l.targetId = :targetId)";

	private static final String COMMON_TASKING_QUERY = "from Tasking t "
			+ "JOIN t.link l where (:missionId is null or t.missionId = :missionId) and (:start is null or "
			+ "t.totTimeMillis >= :start) and (:stop is null or t.totTimeMillis <= :stop) and "
			+ "(:targetId is null or l.targetId = :targetId)";

	static final String FIND_COLLECTION_WINDOW_QUERY = "Select c " + COMMON_COLLECTION_WINDOW_QUERY;

	static final String COUNT_COLLECTION_WINDOW_QUERY = "Select count(*) " + COMMON_COLLECTION_WINDOW_QUERY;

	static final String FIND_TASKING_QUERY = "Select t " + COMMON_TASKING_QUERY;

	static final String COUNT_TASKING_QUERY = "Select count(*) " + COMMON_TASKING_QUERY;
}
