package com.maxar.planning.czml;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.planning.model.czml.TaskingCzmlProperties;
import com.maxar.planning.model.tasking.TaskingModel;

public class TaskingListCzmlTypeHandler implements
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
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> handle(
			final Object object ) {
		return ((List<TaskingModel>) object).stream()
				.map(taskingModel -> taskingModel.produceCzml(properties))
				.collect(Collectors.toList());
	}
}
