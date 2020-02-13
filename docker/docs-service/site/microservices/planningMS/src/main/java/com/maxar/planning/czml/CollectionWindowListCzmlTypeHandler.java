package com.maxar.planning.czml;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.planning.model.image.CollectionWindowModel;
import com.maxar.planning.model.czml.ImageFrameCzmlProperties;

public class CollectionWindowListCzmlTypeHandler implements
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
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> handle(
			final Object object ) {
		final String id = UUID.randomUUID()
				.toString();

		final List<String> czmls = ((List<CollectionWindowModel>) object).stream()
				.map(collectionWindowModel -> collectionWindowModel.produceCzml(id,
																				properties))
				.collect(Collectors.toList());

		czmls.add(	0,
					Packet.create()
							.id(id)
							.name("Collection Windows")
							.writeString());

		return czmls;
	}
}
