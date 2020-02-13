package com.maxar.mission.czml;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.mission.model.TrackModel;
import com.maxar.mission.model.czml.TrackCzmlProperties;

public class TrackCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	TrackCzmlProperties properties;

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return TrackModel.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return false;
	}

	@Override
	public List<String> handle(
			final Object object ) {
		return Collections.singletonList(((TrackModel) object).produceCzml(properties));
	}
}
