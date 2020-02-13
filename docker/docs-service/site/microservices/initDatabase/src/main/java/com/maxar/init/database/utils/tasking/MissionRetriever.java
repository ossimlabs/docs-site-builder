package com.maxar.init.database.utils.tasking;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.maxar.mission.model.MissionModel;
import com.maxar.mission.entity.Mission;
import com.maxar.mission.repository.MissionRepository;
import com.radiantblue.analytics.core.log.SourceLogger;

@Component
public class MissionRetriever
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	@Autowired
	private MissionRepository missionRepository;

	@Transactional
	public List<MissionModel> getMissions(
			final Integer assetId,
			final DateTime beginTime,
			final DateTime endTime ) {
		// get missions for assets in timeframe
		final List<Mission> missions = missionRepository
				.findByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(	assetId
						.toString(),
																											beginTime
																													.getMillis(),
																											endTime.getMillis());

		if ((missions == null) || missions.isEmpty()) {
			logger.warn("No missions found for asset " + assetId + " in time range " + beginTime + " - " + endTime);
			return null;
		}
		final List<MissionModel> missionModels = missions.stream()
				.collect(Collectors.mapping(Mission::toModel,
											Collectors.toList()));

		return missionModels;
	}
}
