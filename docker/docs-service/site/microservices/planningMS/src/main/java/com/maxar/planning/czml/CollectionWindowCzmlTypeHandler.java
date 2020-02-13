package com.maxar.planning.czml;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.planning.model.image.CollectionWindowModel;
import com.maxar.planning.model.czml.ImageFrameCzmlProperties;

public class CollectionWindowCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	ImageFrameCzmlProperties properties;

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return CollectionWindowModel.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return false;
	}

	@Override
	public List<String> handle(
			final Object object ) {

		List<String> czmls = new ArrayList<>();
		final String czml = ((CollectionWindowModel) object).produceCzml(properties);

		czmls.add(0,czml);

		return czmls;
	}
}
