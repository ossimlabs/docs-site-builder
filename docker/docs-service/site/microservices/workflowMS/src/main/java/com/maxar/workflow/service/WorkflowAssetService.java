package com.maxar.workflow.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.maxar.asset.model.IdList;
import com.maxar.asset.model.NameList;
import com.maxar.asset.model.SpaceAssetModel;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.isr.core.model.payload.IPayload;
import com.radiantblue.analytics.isr.core.model.sensor.ISensor;

/**
 * Handles interaction for each asset endpoint of the Workflow service.
 */
@Component
public class WorkflowAssetService
{
	private static Logger logger = SourceLogger.getLogger(WorkflowAssetService.class.getName());

	@Autowired
	private ApiService apiService;

	public NameList getSpaceAssetNames() {
		return apiService.getSpaceAssetNames();
	}

	public IdList getSpaceAssetsIDs() {
		return apiService.getSpaceAssetsIDs();
	}

	public SpaceAssetModel getSpaceAssetModelWithID(
			String id ) {
		return apiService.getSpaceAssetModel(id);
	}

	public SortedMap<String, List<String>> getSpaceSensorTypesWithNames(
			final List<String> sensorTypesNeeded ) {
		return fetchSensorTypesWithNamesOrIds(	true,
												sensorTypesNeeded);
	}

	public SortedMap<String, List<String>> getSpaceSensorTypesWithIds(
			final List<String> sensorTypesNeeded ) {
		return fetchSensorTypesWithNamesOrIds(	false,
												sensorTypesNeeded);
	}

	private SortedMap<String, List<String>> fetchSensorTypesWithNamesOrIds(
			final boolean withNames,
			final List<String> sensorTypesInclude ) {
		TreeMap<String, List<String>> assets = new TreeMap<>();
		final IdList ids = getSpaceAssetsIDs();

		for (String assetID : ids.getIds()) {
			final SpaceAssetModel model = getSpaceAssetModelWithID(assetID);

			try {
				final GenericXmlApplicationContext appContext = new GenericXmlApplicationContext();
				appContext.refresh();
				final Resource assetXmlResource = new ByteArrayResource(
						model.getModelXml()
								.getBytes());
				appContext.load(assetXmlResource);
				final Asset asset = appContext.getBean(Asset.class);
				asset.init();

				List<ISensor> sensors = asset.getPayloads()
						.stream()
						.map(IPayload::sensors)
						.flatMap(Collection::stream)
						.collect(Collectors.toList());

				if (sensorTypesInclude != null && !sensorTypesInclude.isEmpty()) {
					sensors = sensors.stream()
							.filter(e -> sensorTypesInclude.contains(e.getSensorType()))
							.collect(Collectors.toList());
				}

				sensors.forEach(sensor -> {
					if (!assets.containsKey(sensor.getSensorType())) {
						assets.put(	sensor.getSensorType(),
									new ArrayList<>());
					}
					if (withNames) {
						assets.get(sensor.getSensorType())
								.add(asset.getName());
					}
					else {
						assets.get(sensor.getSensorType())
								.add(String.valueOf(asset.getId()));
					}
				});
			}
			catch (final Exception e) {
				logger.error(e);
			}
		}

		return assets;
	}

}
