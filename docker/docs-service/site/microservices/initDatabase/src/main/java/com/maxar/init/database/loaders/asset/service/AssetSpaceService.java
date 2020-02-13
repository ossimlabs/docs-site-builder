package com.maxar.init.database.loaders.asset.service;

import org.springframework.stereotype.Component;

import com.maxar.asset.entity.AssetType;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.isr.core.model.asset.Satellite;

@Component
public class AssetSpaceService extends
		AssetService
{
	@Override
	protected Asset xmlToAsset(
			String xml ) {
		return beanFactoryFromXml(xml).getBean(Satellite.class);
	}

	@Override
	protected AssetType getAssetType() {
		return AssetType.SPACE;
	}
}
