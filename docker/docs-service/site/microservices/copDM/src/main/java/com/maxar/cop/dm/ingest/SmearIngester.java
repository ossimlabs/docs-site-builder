package com.maxar.cop.dm.ingest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxar.asset.model.AssetSmearWithBeamsRequest;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.packet.Polygon;
import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;

public abstract class SmearIngester extends
		CopIngester
{
	protected final Set<String> smearPacketIds = new HashSet<>();

	protected void updateSmearCzml(
			final UriComponentsBuilder smearBeamsUriBuilder,
			final AssetSmearWithBeamsRequest request,
			final String assetId,
			final DateTime stop ) {
		// retrieve CZML to inspect for data to be deleted before posting through to
		// client
		final List<JsonNode> czmlNodes = serviceClient
				.retrieveCzml(cesiumSession.createCzmlRequest(	smearBeamsUriBuilder.toUriString(),
																request,
																assetId,
																false));

		// Identify packet IDs do be deleted within the generated czml
		for (final JsonNode node : czmlNodes) {
			final String id = node.get("id")
					.asText();
			if (id.endsWith(" FOR Smear")) {
				// Ignore this, it's a parent container packet
			}
			// We're cleaning up the smear below by interval, not whole packet
			else if (id.endsWith(" Smear")) {
				smearPacketIds.add(id);
			}
			else {
				deletablePacketIds.add(new TimeTaggedValue<>(
						stop,
						id));
			}
		}

		// post inspected CZML to client
		serviceClient.postPackets(	czmlNodes,
									assetId);
	}

	@Override
	public void cleanupStaleDataWithinPackets(
			final List<String> assetIds,
			final Interval deleteInterval ) {

		final List<JsonNode> deletePackets = new ArrayList<>();

		for (final String assetId : assetIds) {
			// position data
			final Packet vehiclePacket = Packet.create()
					.id(assetId + " - position")
					.position(Property.deleteInterval(deleteInterval));

			deletePackets.add(vehiclePacket.toJsonNode());

			final Packet groundTracePacket = Packet.create()
					.id(assetId + " - ground trace")
					.position(Property.deleteInterval(deleteInterval));

			deletePackets.add(groundTracePacket.toJsonNode());
		}

		// asset smear
		for (final String smearPacketId : smearPacketIds) {
			final Packet assetSmearPacket = Packet.create()
					.id(smearPacketId)
					.polygon(Polygon.create()
							.positions(Property.deleteInterval(deleteInterval)));

			deletePackets.add(assetSmearPacket.toJsonNode());
		}

		serviceClient.postPackets(	deletePackets,
									null);
	}
}
