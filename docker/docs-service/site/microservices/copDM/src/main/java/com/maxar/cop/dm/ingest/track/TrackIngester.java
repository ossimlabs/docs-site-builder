package com.maxar.cop.dm.ingest.track;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.maxar.asset.model.AssetSmearWithBeamsRequest;
import com.maxar.cop.dm.cesium.CesiumTreeNode;
import com.maxar.cop.dm.ingest.SmearIngester;

@Component
public class TrackIngester extends
		SmearIngester
{
	private static final String AIR_VEHICLE_NODE = "Airborne";

	@Value("${microservices.copdm.trackCzmlUrl}")
	private String trackCzmlUrl;

	@Value("${microservices.copdm.airborneSmearBeamsCzmlUrl}")
	private String airborneSmearBeamsCzmlUrl;

	@Value("${microservices.copdm.samplingIntervalMillis}")
	private int samplingIntervalMillis;

	@Override
	protected List<String> getAssetIds() {
		return serviceClient.getAirborneAssetIDs();
	}

	@Override
	protected void updateCzmlData(
			final String assetId,
			final DateTime start,
			final DateTime stop ) {

		final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(trackCzmlUrl)
				.queryParam("assetId",
							assetId)
				.queryParam("startDateString",
							start.toString())
				.queryParam("endDateString",
							stop.toString())
				.queryParam("samplingInterval_ms",
							samplingIntervalMillis)
				.queryParam("format",
							"czml");

		logger.info("Updating COP track data from URL: " + uriBuilder.toUriString());

		serviceClient.postCzmlToUI(cesiumSession.createCzmlRequest(	uriBuilder.toUriString(),
																	null,
																	AIR_VEHICLE_NODE));

		final AssetSmearWithBeamsRequest request = new AssetSmearWithBeamsRequest();
		request.setAssetId(assetId);
		request.setStartTimeISO8601(start.toString());
		request.setStopTimeISO8601(stop.toString());
		request.setForFrameIncrementSec(samplingIntervalMillis / 1000);

		final UriComponentsBuilder smearBeamsUriBuilder = UriComponentsBuilder.fromHttpUrl(airborneSmearBeamsCzmlUrl)
				.queryParam("format",
							"czml");

		updateSmearCzml(smearBeamsUriBuilder,
						request,
						assetId,
						stop);
	}

	@Override
	protected void createInitialTreeNodes() {
		final CesiumTreeNode airborneVehicles = new CesiumTreeNode(
				AIR_VEHICLE_NODE,
				AIR_VEHICLE_NODE,
				VEHICLE_NODE);
		cesiumSession.addTreeNode(airborneVehicles);

		final List<String> assets = getAssetIds();
		for (final String assetId : assets) {
			cesiumSession.addTreeNode(new CesiumTreeNode(
					assetId,
					assetId,
					AIR_VEHICLE_NODE));
		}
	}
}
