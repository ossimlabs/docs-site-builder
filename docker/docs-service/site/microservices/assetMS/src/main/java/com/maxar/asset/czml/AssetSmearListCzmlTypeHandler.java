package com.maxar.asset.czml;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.asset.model.AssetSmear;
import com.maxar.asset.model.czml.AssetSmearCzmlProperties;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.common.czml.CzmlTypeHandler;

public class AssetSmearListCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	AssetSmearCzmlProperties properties;

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return AssetSmear.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> handle(
			final Object object ) {
		final boolean createRootNode = properties.isCreateSmearRootNode();
		final String id = createRootNode ? ((List<AssetSmear>) object).get(0)
				.getAsset()
				.getName() + " FOR Smear" : null;

		final List<String> czmls = ((List<AssetSmear>) object).stream()
				.map(assetSmear -> assetSmear.produceCzml(	id,
															properties))
				.collect(Collectors.toList());

		if (createRootNode) {
			czmls.add(	0,
						Packet.create()
								.id(id)
								.name(id)
								.writeString());
		}

		return czmls;
	}
}
