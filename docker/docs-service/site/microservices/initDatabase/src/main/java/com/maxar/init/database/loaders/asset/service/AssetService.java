package com.maxar.init.database.loaders.asset.service;

import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionStoreException;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import com.maxar.asset.entity.AssetType;
import com.maxar.asset.repository.AssetRepository;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.isr.core.model.asset.Asset;

public abstract class AssetService
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	@Autowired
	private AssetRepository assetRepository;

	public void createAsset(
			final String assetXml ) {
		try {
			final Asset asset = xmlToAsset(assetXml);

			final String assetId = Integer.toString(asset.getId());

			if (assetRepository.findById(assetId)
					.isPresent()) {
				logger.error("Asset ID already exists");
				return;
			}

			final String assetName = asset.getName();

			if (assetRepository.getByName(assetName)
					.isPresent()) {
				logger.error("Asset name already exists");
				return;
			}

			instantiateAndSaveAsset(assetId,
									asset,
									assetXml);
		}
		catch (final XmlBeanDefinitionStoreException e) {
			logger.error("XmlBeanDefinitionStoreException create space asset");
		}
	}

	protected abstract Asset xmlToAsset(
			final String xml );

	protected abstract AssetType getAssetType();

	protected static BeanFactory beanFactoryFromXml(
			final String xml ) {
		final Resource resource = new ByteArrayResource(
				xml.getBytes(StandardCharsets.UTF_8));

		return new GenericXmlApplicationContext(
				resource);
	}

	private void instantiateAndSaveAsset(
			final String id,
			final Asset asset,
			final String xml ) {
		final com.maxar.asset.entity.Asset persistedAsset = new com.maxar.asset.entity.Asset();
		persistedAsset.setId(id);
		persistedAsset.setName(asset.getName());
		persistedAsset.setXml(xml);
		persistedAsset.setType(getAssetType());

		assetRepository.save(persistedAsset);
	}
}
