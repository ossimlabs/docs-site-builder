package com.maxar.airborne.access.czml;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.access.model.UntrimmedAccess;
import com.maxar.access.model.czml.AccessCzmlProperties;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.common.czml.CzmlTypeHandler;

public class AccessListCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	AccessCzmlProperties properties;

	private static final String PARENT_ID = "Airborne Accesses";

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return UntrimmedAccess.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> handle(
			final Object object ) {
		final List<String> czmls = ((List<UntrimmedAccess>) object).stream()
				.map(untrimmedAccess -> untrimmedAccess.produceCzml(PARENT_ID,
																	properties))
				.collect(Collectors.toList());

		czmls.add(	0,
					Packet.create()
							.id(PARENT_ID)
							.name(PARENT_ID)
							.writeString());

		return czmls;
	}
}
