package com.maxar.asset.common.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.exception.AssetNameDoesNotExistException;
import com.maxar.asset.common.io.InMemoryResourceLocation;
import com.maxar.asset.model.ExtraData;
import com.radiantblue.analytics.isr.core.model.asset.Asset;

public abstract class AssetRetrieverAbstract implements
		AssetRetriever
{
	@Autowired
	protected ApiService apiService;

	public Asset getAssetModelByName(
			final String name )
			throws AssetNameDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final String assetId = getAssetIdByName(name);
		final String assetXml = getAssetById(assetId);
		final List<String> extraDataNames = getAllAssetExtraDataNames(assetId);
		final List<String> extraXml = new ArrayList<>();
		for (final String extraName : extraDataNames) {
			final ExtraData extraData = getAssetExtraDataByName(assetId,
																extraName);
			// skip 'null' extraData
			if (extraData != null) {
				if (extraData.getType()
						.equalsIgnoreCase("XML")) {
					extraXml.add(extraData.getData());
				}
				else if (extraData.getType()
						.equals(InMemoryResourceLocation.class.getName())) {
					InMemoryResourceLocation.addToResourceMap(	extraName,
																extraData.getData()
																		.getBytes());
				}
			}
		}

		final Asset asset = buildAssetFromXml(	name,
												assetXml,
												extraXml);
		return asset;
	}

	private Asset buildAssetFromXml(
			final String assetName,
			final String modelXml,
			final List<String> extraXml ) {
		try (final GenericXmlApplicationContext appContext = new GenericXmlApplicationContext();) {
			final Resource assetXmlResource = new ByteArrayResource(
					modelXml.getBytes());
			appContext.load(assetXmlResource);

			if (extraXml != null) {
				for (final String extraXmlString : extraXml) {
					final Resource extraXmlResource = new ByteArrayResource(
							extraXmlString.getBytes());
					appContext.load(extraXmlResource);
				}
			}

			appContext.refresh();

			final Asset asset = (Asset) appContext.getBean(assetName);
			asset.init();
			return asset;
		}
	}
}
