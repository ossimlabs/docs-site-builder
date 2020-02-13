package com.maxar.init.database.loaders.asset.service;

import org.springframework.stereotype.Component;

import com.maxar.asset.entity.AssetType;
import com.radiantblue.analytics.isr.core.model.asset.Aircraft;
import com.radiantblue.analytics.isr.core.model.asset.Asset;

@Component
public class AssetAirborneService extends
		AssetService
{
	@Override
	protected Asset xmlToAsset(
			String xml ) {
		return beanFactoryFromXml(xml).getBean(Aircraft.class);
	}

	@Override
	protected AssetType getAssetType() {
		return AssetType.AIRBORNE;
	}
}
