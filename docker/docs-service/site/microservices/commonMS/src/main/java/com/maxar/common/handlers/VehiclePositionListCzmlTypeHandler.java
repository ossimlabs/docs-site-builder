package com.maxar.common.handlers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.common.czml.VehiclePositionCZMLUtils;
import com.maxar.common.czml.VehiclePositionCzmlProperties;
import com.maxar.common.types.VehiclePosition;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

public class VehiclePositionListCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	VehiclePositionCzmlProperties properties;

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return VehiclePosition.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> handle(
			final Object object ) {
		final String id = ((List<VehiclePosition>) object).get(0)
				.getId();
		final List<StateVectorsInFrame> svifs = ((List<VehiclePosition>) object).stream()
				.map(vh -> vh.getSvif())
				.collect(Collectors.toList());
		final String czml = VehiclePositionCZMLUtils.writeStateVectorsToCZML(	svifs,
																				id,
																				properties);

		return Collections.singletonList(czml);
	}
}
