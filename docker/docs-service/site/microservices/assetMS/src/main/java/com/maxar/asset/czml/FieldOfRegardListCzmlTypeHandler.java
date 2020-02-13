package com.maxar.asset.czml;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.asset.model.FieldOfRegard;
import com.maxar.asset.model.czml.FieldOfRegardCzmlProperties;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.common.czml.CzmlTypeHandler;

public class FieldOfRegardListCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	FieldOfRegardCzmlProperties properties;

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return FieldOfRegard.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> handle(
			final Object object ) {
		final String id = UUID.randomUUID()
				.toString();

		final List<String> czmls = ((List<FieldOfRegard>) object).stream()
				.map(fieldOfRegard -> fieldOfRegard.produceCzml(id,
																properties))
				.collect(Collectors.toList());

		czmls.add(	0,
					Packet.create()
							.id(id)
							.name("Field Of Regard")
							.writeString());

		return czmls;
	}
}
