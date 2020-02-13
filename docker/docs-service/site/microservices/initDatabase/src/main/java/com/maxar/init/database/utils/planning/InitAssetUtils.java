package com.maxar.init.database.utils.planning;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.io.InMemoryResourceLocation;
import com.maxar.asset.entity.Asset;
import com.maxar.asset.entity.AssetExtraData;
import com.maxar.asset.entity.AssetType;
import com.maxar.asset.repository.AssetRepository;

@Component
public class InitAssetUtils
{
	@Autowired
	private AssetRepository assetRepository;

	public List<Asset> getSpaceAssets() {
		final List<Asset> assets = assetRepository.getByType(AssetType.SPACE)
				.stream()
				.collect(Collectors.toList());

		return assets;
	}

	@Transactional
	public List<com.radiantblue.analytics.isr.core.model.asset.Asset> buildSpaceAssets(
			final List<Asset> rawAssets ) {
		return rawAssets.stream()
				.map(asset -> buildAssetFromRaw(asset,
												AssetType.SPACE))
				.collect(Collectors.toList());
	}

	@Transactional
	private AssetExtraData getAssetExtraDataByName(
			final String assetId,
			final AssetType assetType,
			final String extraName ) {
		try {
			return assetRepository.getByIdAndType(	assetId,
													assetType)
					.map(Asset::getExtraData)
					.orElseThrow(() -> new AssetIdDoesNotExistException(
							assetId))
					.stream()
					.filter(assetExtraData -> extraName.equals(assetExtraData.getName()))
					.findFirst()
					.orElseThrow(() -> new AssetExtraDataDoesNotExistException(
							assetId,
							extraName));
		}
		catch (AssetExtraDataDoesNotExistException | AssetIdDoesNotExistException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Transactional
	private List<String> getAllAssetExtraDataNames(
			final String assetId,
			final AssetType assetType ) {
		try {
			return assetRepository.getByIdAndType(	assetId,
													assetType)
					.map(Asset::getExtraData)
					.orElseThrow(() -> new AssetIdDoesNotExistException(
							assetId))
					.stream()
					.map(AssetExtraData::getName)
					.collect(Collectors.toList());
		}
		catch (final AssetIdDoesNotExistException e) {
			e.printStackTrace();
			return null;
		}
	}

	private com.radiantblue.analytics.isr.core.model.asset.Asset buildAssetFromRaw(
			final Asset rawAsset,
			final AssetType assetType ) {
		// lazy loading issue with extra data
		final List<String> extraDataNames = getAllAssetExtraDataNames(	rawAsset.getId(),
																		assetType);
		try (final GenericXmlApplicationContext appContext = new GenericXmlApplicationContext();) {
			final Resource assetXmlResource = new ByteArrayResource(
					rawAsset.getXml()
							.getBytes());
			appContext.load(assetXmlResource);

			if (extraDataNames != null) {
				for (final String extraDataName : extraDataNames) {
					final AssetExtraData extraData = getAssetExtraDataByName(	rawAsset.getId(),
																				assetType,
																				extraDataName);
					if (extraData != null) {
						if (extraData.getType()
								.equalsIgnoreCase("XML")) {
							final Resource extraXmlResource = new ByteArrayResource(
									new String(
											extraData.getRawData()).getBytes());
							appContext.load(extraXmlResource);
						}
						else if (extraData.getType()
								.equals(InMemoryResourceLocation.class.getName())) {
							InMemoryResourceLocation.addToResourceMap(	extraData.getName(),
																		new String(
																				extraData.getRawData()).getBytes());
						}
					}
				}
			}

			appContext.refresh();

			final com.radiantblue.analytics.isr.core.model.asset.Asset asset = (com.radiantblue.analytics.isr.core.model.asset.Asset) appContext
					.getBean(rawAsset.getName());
			asset.init();
			return asset;
		}
	}

	public List<Asset> getAirborneAssets() {
		final List<Asset> assets = assetRepository.getByType(AssetType.AIRBORNE)
				.stream()
				.collect(Collectors.toList());

		return assets;
	}

	@Transactional
	public List<com.radiantblue.analytics.isr.core.model.asset.Asset> buildAirborneAssets(
			final List<Asset> rawAirborneAssets ) {
		return rawAirborneAssets.stream()
				.map(asset -> buildAssetFromRaw(asset,
												AssetType.AIRBORNE))
				.collect(Collectors.toList());
	}
}
