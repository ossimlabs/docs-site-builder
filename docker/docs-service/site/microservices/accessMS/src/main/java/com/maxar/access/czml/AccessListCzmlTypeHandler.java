package com.maxar.access.czml;

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
	private static final String SPACE_ACCESSES = "Space Accesses";

	@Autowired
	AccessCzmlProperties properties;

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
				.map(untrimmedAccess -> untrimmedAccess.produceCzml(SPACE_ACCESSES,
																	properties))
				.collect(Collectors.toList());

		czmls.add(	0,
					Packet.create()
							.id(SPACE_ACCESSES)
							.name(SPACE_ACCESSES)
							.writeString());

		return czmls;
	}
}
