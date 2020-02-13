package com.maxar.planning.model.image;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.Interval;

import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.planning.model.czml.ImageFrameCzmlProperties;
import com.radiantblue.analytics.core.DateTimeFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionWindowModel
{
	private String cwId;
	private String status;
	private String assetName;
	private Integer assetScn;
	private Long startMillis;
	private Long endMillis;

	@Builder.Default
	private Set<ImageOpModel> imageOps = new HashSet<>();

	public String produceCzml() {
		return produceCzml(	null,
							new ImageFrameCzmlProperties());
	}

	public String produceCzml(
			final ImageFrameCzmlProperties properties ) {
		return produceCzml(	null,
							properties);
	}

	public String produceCzml(
			final String parentId,
			final ImageFrameCzmlProperties properties ) {
		final String id = "CW " + cwId + " Asset " + assetName;

		Packet packet = Packet.create()
				.id(id)
				.name("CW " + cwId + " Asset " + assetName);

		if (parentId != null) {
			packet = packet.parent(parentId);
		}

		if ((startMillis != null) && (endMillis != null)) {
			final Interval packetInterval = new Interval(
					DateTimeFactory.fromMillis(startMillis),
					DateTimeFactory.fromMillis(endMillis));

			packet = packet.availability(	packetInterval.getStart(),
											packetInterval.getEnd());
		}

		final List<String> imageOpCzmlsList = new ArrayList<>();

		if ((imageOps != null) && !imageOps.isEmpty()) {
			imageOpCzmlsList.addAll(imageOps.stream()
					.map(imageOpModel -> imageOpModel.produceCzml(	id,
																	assetName,
																	properties))
					.collect(Collectors.toList()));

			imageOpCzmlsList.add(	0,
									packet.writeString());

			return imageOpCzmlsList.stream()
					.collect(Collectors.joining(","));
		}
		else {
			return packet.writeString();
		}
	}
}
