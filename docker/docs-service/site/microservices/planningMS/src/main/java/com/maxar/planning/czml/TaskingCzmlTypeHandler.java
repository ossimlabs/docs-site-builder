package com.maxar.planning.czml;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.planning.model.czml.TaskingCzmlProperties;
import com.maxar.planning.model.tasking.TaskingModel;

public class TaskingCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	TaskingCzmlProperties properties;

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return TaskingModel.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return false;
	}

	@Override
	public List<String> handle(
			final Object object ) {
		return Collections.singletonList(((TaskingModel) object).produceCzml(properties));
	}
}
