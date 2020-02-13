package com.maxar.cop.dm.ingest.planning;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxar.asset.model.AssetSmearWithBeamsRequest;
import com.maxar.asset.model.OpBeam;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.maxar.cop.dm.cesium.CesiumTreeNode;
import com.maxar.cop.dm.ingest.CopIngester;

@Component
public class AirbornePlanIngester extends
		CopIngester
{

	private static final String AIRBORNE_PLAN_NODE = "Airborne Collections";

	@Value("${microservices.copdm.airborneTaskingCzmlUrl}")
	private String airborneTaskingCzmlUrl;

	@Value("${microservices.copdm.airborneSmearBeamsCzmlUrl}")
	private String airborneSmearBeamsCzmlUrl;

	@Override
	protected void updateCzmlData(
			final String assetId,
			final DateTime start,
			final DateTime stop ) {

		final List<String> missionIds = serviceClient.getAirborneMissionIDs(assetId,
																			start,
																			stop);

		final String parentId = assetId + " - Taskings";

		final List<JsonNode> czmlNodes = new ArrayList<>();

		for (final String missionId : missionIds) {

			cesiumSession.ensureParentNodeExists(new CesiumTreeNode(
					parentId,
					parentId,
					AIRBORNE_PLAN_NODE));

			final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(airborneTaskingCzmlUrl)
					.queryParam("missionId",
								missionId)
					.queryParam("start",
								start.toString())
					.queryParam("stop",
								stop.toString())
					.queryParam("format",
								"czml");

			logger.info("Updating COP airborne tasking data from URL: " + uriBuilder.toUriString());

			czmlNodes.addAll(serviceClient.retrieveCzml(cesiumSession.createCzmlRequest(uriBuilder.toUriString(),
																						null,
																						parentId)));

			final List<OpBeam> ops = serviceClient.getAirborneOps(	missionId,
																	start,
																	stop);

			if ((ops != null) && !ops.isEmpty()) {
				final AssetSmearWithBeamsRequest request = new AssetSmearWithBeamsRequest();
				request.setAssetId(assetId);
				request.setStartTimeISO8601(start.toString());
				request.setStopTimeISO8601(stop.toString());
				request.setBeams(ops);
				request.setSmearCzmlRequested(false);
				request.setForFramesCzmlRequested(false);
				request.setOpBeamsCzmlRequested(true);

				final UriComponentsBuilder smearBeamsUriBuilder = UriComponentsBuilder
						.fromHttpUrl(airborneSmearBeamsCzmlUrl)
						.queryParam("format",
									"czml");
				czmlNodes.addAll(serviceClient
						.retrieveCzml(cesiumSession.createCzmlRequest(	smearBeamsUriBuilder.toUriString(),
																		request,
																		parentId)));
			}

			// Identify deletable packet IDs within the generated czml
			for (final JsonNode node : czmlNodes) {
				final String id = node.get("id")
						.asText();
				// Delete the whole CW of ops and beams after it's been over for the data
				// lifespan
				deletablePacketIds.add(new TimeTaggedValue<>(
						stop,
						id));
			}

			// post inspected CZML to client
			serviceClient.postPackets(	czmlNodes,
										parentId);
		}
	}

	@Override
	protected List<String> getAssetIds() {
		return serviceClient.getAirborneAssetIDs();
	}

	@Override
	protected void createInitialTreeNodes() {
		final CesiumTreeNode airbornePlan = new CesiumTreeNode(
				AIRBORNE_PLAN_NODE,
				AIRBORNE_PLAN_NODE,
				PLAN_NODE);
		cesiumSession.addTreeNode(airbornePlan);
	}

	@Override
	public void cleanupStaleDataWithinPackets(
			final List<String> assetIds,
			final Interval deleteInterval ) {
		// This ingester's data is all deleted at the packet level
	}

}
