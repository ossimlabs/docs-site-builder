package com.maxar.cop.dm.ingest.planning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.maxar.planning.model.image.CollectionWindowModel;

@Component
public class SpacePlanIngester extends
		CopIngester
{

	private static final String SPACE_PLAN_NODE = "Space Collections";

	@Value("${microservices.copdm.collectionWindowCzmlUrl}")
	private String collectionWindowCzmlUrl;

	@Value("${microservices.copdm.spaceSmearBeamsCzmlUrl}")
	private String spaceSmearBeamsCzmlUrl;

	private final Set<String> collectionWindowsDisplayed = new HashSet<>();

	@Override
	protected void updateCzmlData(
			final String assetId,
			final DateTime start,
			final DateTime stop ) {

		final String parentId = assetId + " - Collection Plan";

		final List<CollectionWindowModel> cws = serviceClient.getCws(	assetId,
																		start,
																		stop);

		final List<JsonNode> czmlNodes = new ArrayList<>();

		if (cws != null) {
			cesiumSession.ensureParentNodeExists(new CesiumTreeNode(
					parentId,
					parentId,
					SPACE_PLAN_NODE));
			for (final CollectionWindowModel cw : cws) {

				final String cwId = cw.getCwId();
				final DateTime cwStart = new DateTime(
						cw.getStartMillis());
				final DateTime cwEnd = new DateTime(
						cw.getEndMillis());

				if (!collectionWindowsDisplayed.contains(cwId)) {
					final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(collectionWindowCzmlUrl)
							.queryParam("cwId",
										cwId)
							.queryParam("scn",
										assetId)
							.queryParam("format",
										"czml");

					logger.info("Updating COP collection window data from URL: " + uriBuilder.toUriString());

					czmlNodes
							.addAll(serviceClient.retrieveCzml(cesiumSession.createCzmlRequest(	uriBuilder.toUriString(),
																								null,
																								parentId)));

					final List<OpBeam> ops = new ArrayList<>();
					cw.getImageOps()
							.forEach(imageOp -> imageOp.getImageFrames()
									.forEach(frame -> {
										final OpBeam op = new OpBeam();
										op.setStartTimeISO8601(new DateTime(
												imageOp.getOpStartTimeMillis()).toString());
										op.setStopTimeISO8601(new DateTime(
												imageOp.getOpEndTimeMillis()).toString());
										op.setBeamCentroidGeo(frame.getCenterCoord());
										ops.add(op);
									}));

					final AssetSmearWithBeamsRequest request = new AssetSmearWithBeamsRequest();
					request.setAssetId(assetId);
					request.setStartTimeISO8601(cwStart.toString());
					request.setStopTimeISO8601(cwEnd.toString());
					request.setBeams(ops);
					request.setSmearCzmlRequested(false);
					request.setForFramesCzmlRequested(false);
					request.setOpBeamsCzmlRequested(true);

					final UriComponentsBuilder smearBeamsUriBuilder = UriComponentsBuilder
							.fromHttpUrl(spaceSmearBeamsCzmlUrl)
							.queryParam("format",
										"czml");
					czmlNodes.addAll(serviceClient
							.retrieveCzml(cesiumSession.createCzmlRequest(	smearBeamsUriBuilder.toUriString(),
																			request,
																			parentId,
																			false)));

					// Identify deletable packet IDs within the generated czml
					for (final JsonNode node : czmlNodes) {
						final String id = node.get("id")
								.asText();
						// Delete the whole CW of ops and beams after it's been over for the data
						// lifespan
						deletablePacketIds.add(new TimeTaggedValue<>(
								cwEnd,
								id));
					}

					// post inspected CZML to client
					serviceClient.postPackets(	czmlNodes,
												parentId);

					collectionWindowsDisplayed.add(cw.getCwId());
				}
			}
		}
	}

	@Override
	protected List<String> getAssetIds() {
		return serviceClient.getSpaceAssetIDs();
	}

	@Override
	protected void createInitialTreeNodes() {
		final CesiumTreeNode plan = new CesiumTreeNode(
				PLAN_NODE,
				PLAN_NODE,
				null);
		final CesiumTreeNode spacePlan = new CesiumTreeNode(
				SPACE_PLAN_NODE,
				SPACE_PLAN_NODE,
				PLAN_NODE);
		cesiumSession.addTreeNode(plan);
		cesiumSession.addTreeNode(spacePlan);
	}

	@Override
	public void cleanupStaleDataWithinPackets(
			final List<String> assetIds,
			final Interval deleteInterval ) {
		// This ingester's data is all deleted at the packet level
	}

}
