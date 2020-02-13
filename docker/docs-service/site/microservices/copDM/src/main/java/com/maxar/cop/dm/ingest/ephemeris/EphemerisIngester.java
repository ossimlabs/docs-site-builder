package com.maxar.cop.dm.ingest.ephemeris;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.maxar.asset.model.AssetSmearWithBeamsRequest;
import com.maxar.cop.dm.cesium.CesiumTreeNode;
import com.maxar.cop.dm.ingest.SmearIngester;

@Component
public class EphemerisIngester extends
		SmearIngester
{
	private static final String SPACE_VEHICLE_NODE = "Space";

	@Value("${microservices.copdm.ephemerisCzmlUrl}")
	private String ephemerisCzmlUrl;

	@Value("${microservices.copdm.spaceSmearBeamsCzmlUrl}")
	private String spaceSmearBeamsCzmlUrl;

	@Value("${microservices.copdm.samplingIntervalMillis}")
	private int samplingIntervalMillis;

	@Value("${microservices.copdm.vehiclePositionDisplayLengthSeconds}")
	private int vehiclePositionDisplayLengthSeconds;

	@Override
	protected void updateCzmlData(
			final String assetId,
			final DateTime start,
			final DateTime stop ) {

		final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(ephemerisCzmlUrl)
				.queryParam("scn",
							assetId)
				.queryParam("startDateString",
							start.toString())
				.queryParam("endDateString",
							stop.toString())
				.queryParam("samplingInterval_ms",
							samplingIntervalMillis)
				.queryParam("format",
							"czml");

		logger.info("Updating COP ephemeris data from URL: " + uriBuilder.toUriString());

		serviceClient.postCzmlToUI(cesiumSession.createCzmlRequest(	uriBuilder.toUriString(),
																	null,
																	SPACE_VEHICLE_NODE));

		final AssetSmearWithBeamsRequest request = new AssetSmearWithBeamsRequest();
		request.setAssetId(assetId);
		request.setStartTimeISO8601(stop.minusSeconds(vehiclePositionDisplayLengthSeconds)
				.toString());
		request.setStopTimeISO8601(stop.toString());
		request.setForFrameIncrementSec(samplingIntervalMillis / 1000);

		final UriComponentsBuilder smearBeamsUriBuilder = UriComponentsBuilder.fromHttpUrl(spaceSmearBeamsCzmlUrl)
				.queryParam("format",
							"czml");

		updateSmearCzml(smearBeamsUriBuilder,
						request,
						assetId,
						stop);
	}

	@Override
	protected List<String> getAssetIds() {
		return serviceClient.getSpaceAssetIDs();
	}

	@Override
	protected void createInitialTreeNodes() {
		final CesiumTreeNode vehicles = new CesiumTreeNode(
				VEHICLE_NODE,
				VEHICLE_NODE,
				null);
		final CesiumTreeNode spaceVehicles = new CesiumTreeNode(
				SPACE_VEHICLE_NODE,
				SPACE_VEHICLE_NODE,
				VEHICLE_NODE);
		cesiumSession.addTreeNode(vehicles);
		cesiumSession.addTreeNode(spaceVehicles);

		final List<String> assets = getAssetIds();
		for (final String assetId : assets) {
			cesiumSession.addTreeNode(new CesiumTreeNode(
					assetId,
					assetId,
					SPACE_VEHICLE_NODE));
		}
	}
}
