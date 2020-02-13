package com.maxar.planning.model.image;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.Interval;

import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.packet.LayerControl;
import com.maxar.planning.model.link.LinkModel;
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
public class ImageOpModel
{
	private String opId;
	private Long opStartTimeMillis;
	private Long opEndTimeMillis;
	private String targetType;
	private String countryCode;
	private String sensorType;
	private String sensorMode;
	private Double niirs;
	private Integer elevation;
	private Double azimuth;
	private Double gsdOrIprMeters;
	private LinkModel link;

	@Builder.Default
	private Set<ImageFrameModel> imageFrames = new HashSet<>();

	public String produceCzml(
			final String parentId,
			final String parentAssetName,
			final ImageFrameCzmlProperties properties ) {
		final String packetId = UUID.randomUUID()
				.toString();
		Packet packet = Packet.create()
				.id(packetId)
				.name("Image Op " + opId)
				.layerControl(LayerControl.create()
						.showAsLayer(properties.isDisplayFrameInTree()));

		if (parentId != null) {
			packet = packet.parent(parentId);
		}

		if ((opStartTimeMillis != null) && (opEndTimeMillis != null)) {
			final Interval packetInterval = new Interval(
					DateTimeFactory.fromMillis(opStartTimeMillis),
					DateTimeFactory.fromMillis(opEndTimeMillis));

			packet = packet.availability(	packetInterval.getStart(),
											packetInterval.getEnd());
		}

		final List<String> imageFrameCzmlList = new ArrayList<>();

		if ((imageFrames != null) && !imageFrames.isEmpty()) {
			imageFrameCzmlList.addAll(imageFrames.stream()
					.map(imageFrameModel -> imageFrameModel.produceCzml(packetId,
																		parentAssetName,
																		properties))
					.collect(Collectors.toList()));

			imageFrameCzmlList.add(	0,
									packet.writeString());

			return imageFrameCzmlList.stream()
					.collect(Collectors.joining(","));
		}
		else {
			return packet.writeString();
		}
	}
}
