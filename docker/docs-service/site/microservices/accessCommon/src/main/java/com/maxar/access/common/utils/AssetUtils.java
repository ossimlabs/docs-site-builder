package com.maxar.access.common.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.maxar.access.common.exception.NoMatchingAssetsException;
import com.maxar.access.common.exception.UnsupportedSensorTypeException;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.isr.core.model.payload.IPayload;
import com.radiantblue.analytics.isr.core.model.sensor.ISensor;
import com.radiantblue.analytics.isr.core.model.sensormode.ISensorMode;

@Component
public class AssetUtils
{
	private static Logger logger = SourceLogger.getLogger(AssetUtils.class.getName());

	private static final String eoSensorType = "EO";
	private static final String irSensorType = "IR";
	private static final String radarSensorType = "RADAR";
	private static Set<String> validSensorTypes = new HashSet<>();
	static {
		Collections.addAll(validSensorTypes,
						   eoSensorType,
						   irSensorType,
						   radarSensorType);
	}

	public static ISensorMode getSensorModeByName(
			final Asset asset,
			final String sensorModeName ) {
		ISensorMode iSensorMode = null;
		for (final IPayload pl : asset.getPayloads()) {
			for (final ISensor s : pl.sensors()) {
				for (final ISensorMode mode : s.modes()) {
					if (mode
							.getName()
							.equals(
									sensorModeName)) {
						iSensorMode = mode;
					}
				}
			}
		}
		return iSensorMode;
	}

	public Asset buildAssetFromModel(
			final String assetName,
			final String modelXml,
			final List<String> extraXml ) {
		try (final GenericXmlApplicationContext appContext = new GenericXmlApplicationContext()) {
			final Resource assetXmlResource = new ByteArrayResource(
					modelXml.getBytes());
			appContext
					.load(
							assetXmlResource);

			if(extraXml != null) {
				for (final String extraXmlString : extraXml) {
					final Resource extraXmlResource = new ByteArrayResource(
							extraXmlString.getBytes());
					appContext
							.load(
									extraXmlResource);
				}
			}

			appContext.refresh();

			final Asset asset = (Asset) appContext
					.getBean(
							assetName);
			asset.init();
			return asset;
		}
	}

	/**
	 * Searches a list of assets for sensor types matching an input sensor type,
	 * and returns only the matching assets.
	 *
	 * @param assets The list of assets to search. This list is not modified.
	 * @param sensorType The sensor type to filter for. If null, no assets will
	 *                   be filtered.
	 * @return A list of all assets in the input list that contain a matching
	 *         sensor type to the one specified.
	 * @throws UnsupportedSensorTypeException Thrown if the sensor type
	 *                                        specified was not null, and was
	 *                                        not recognized.
	 */
	public static List<Asset> filterAssetListBySensorType(final List<Asset> assets,
														  final String sensorType) throws
			UnsupportedSensorTypeException,
			NoMatchingAssetsException {
		if (sensorType == null) {
			return assets;
		}

		if (!validSensorTypes.contains(sensorType)) {
			logger.error("Unsupported sensor type: " + sensorType);

			throw new UnsupportedSensorTypeException(sensorType);
		}

		final List<Asset> filteredAssets = assets.stream()
				.filter(asset -> assetHasSensorType(asset,
													sensorType))
				.collect(Collectors.toList());

		if (filteredAssets.isEmpty()) {
			logger.error("No assets match the provided sensor type.");

			throw new NoMatchingAssetsException(sensorType);
		}

		return filteredAssets;
	}

	/**
	 * @param asset The asset to search.
	 * @param sensorType The sensor type to search for.
	 * @return True if any payload has any sensor with a sensor type matching
	 *         the input sensor type.
	 */
	private static boolean assetHasSensorType(final Asset asset,
											  final String sensorType) {
		return asset.getPayloads()
				.stream()
				.map(IPayload::sensors)
				.flatMap(Collection::stream)
				.anyMatch(sensor -> sensor.getSensorType().equals(sensorType));
	}
}
