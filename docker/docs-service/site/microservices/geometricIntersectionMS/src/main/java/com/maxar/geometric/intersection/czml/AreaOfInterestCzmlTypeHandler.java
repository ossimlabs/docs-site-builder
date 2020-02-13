package com.maxar.geometric.intersection.czml;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.geometric.intersection.model.AreaOfInterest;

public class AreaOfInterestCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	private AreaOfInterestCzmlProducer areaOfInterestCzmlProducer;

	@Autowired
	private AreaOfInterestCzmlProperties properties;

	@Override
	public boolean canHandle(
			final Class<?> clazz ) {
		return AreaOfInterest.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return false;
	}

	@Override
	public List<String> handle(
			final Object object ) {
		final AreaOfInterest aoi = (AreaOfInterest) object;
		return Collections.singletonList(areaOfInterestCzmlProducer.produceCzml(aoi,
																				properties));
	}
}
