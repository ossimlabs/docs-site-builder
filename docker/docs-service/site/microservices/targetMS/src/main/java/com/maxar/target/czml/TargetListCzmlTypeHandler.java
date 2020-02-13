package com.maxar.target.czml;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.target.model.TargetModel;
import com.maxar.target.model.czml.TargetCzmlProperties;

public class TargetListCzmlTypeHandler implements
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
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> handle(
			final Object object ) {
		final String id = UUID.randomUUID()
				.toString();

		final List<String> czmls = ((List<TargetModel>) object).stream()
				.map(targetModel -> targetModel.produceCzml(id,
															properties))
				.collect(Collectors.toList());

		czmls.add(	0,
					Packet.create()
							.id(id)
							.name("Requirements")
							.writeString());

		return czmls;
	}
}
