package com.maxar.asset.service;

import org.joda.time.DateTime;

import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;
import com.radiantblue.analytics.isr.core.model.asset.IAsset;

public class AssetFORFrameProducer implements
		FORFrameProducer
{
	private final IAsset asset;

	AssetFORFrameProducer(
			final IAsset asset ) {
		this.asset = asset;
	}

	@Override
	public GeodeticGeometry getFOR(
			final DateTime atTime ) {
		return asset.getFOR(atTime);
	}

	@Override
	public String getName() {
		return asset.getName();
	}

	@Override
	public String getSensorType() {
		return ""; // no sensor type at the asset level
	}
}
