package com.maxar.common.handlers;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.common.czml.VehiclePositionCZMLUtils;
import com.maxar.common.czml.VehiclePositionCzmlProperties;
import com.maxar.common.types.VehiclePosition;

public class VehiclePositionCzmlTypeHandler implements
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
		return false;
	}

	@Override
	public List<String> handle(
			final Object object ) {
		return Collections.singletonList(VehiclePositionCZMLUtils
				.writeStateVectorsToCZML(	Collections.singletonList(((VehiclePosition) object).getSvif()),
											((VehiclePosition) object).getId(),
											properties));
	}
}
