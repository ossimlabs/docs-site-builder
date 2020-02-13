package com.maxar.target.czml;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.target.model.TargetModel;
import com.maxar.target.model.czml.TargetCzmlProperties;

public class TargetCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	TargetCzmlProperties properties;

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return TargetModel.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return false;
	}

	@Override
	public List<String> handle(
			final Object object ) {
		return Collections.singletonList(((TargetModel) object).produceCzml(properties));
	}
}
