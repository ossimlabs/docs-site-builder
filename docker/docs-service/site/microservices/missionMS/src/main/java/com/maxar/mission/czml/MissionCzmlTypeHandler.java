package com.maxar.mission.czml;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.mission.model.MissionModel;
import com.maxar.mission.model.czml.TrackCzmlProperties;

public class MissionCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	TrackCzmlProperties properties;

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return MissionModel.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return false;
	}

	@Override
	public List<String> handle(
			final Object object ) {
		return Collections.singletonList(((MissionModel) object).produceCzml(properties));
	}
}
