package com.maxar.mission.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.Interval;

import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.mission.model.czml.TrackCzmlProperties;
import com.radiantblue.analytics.core.DateTimeFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MissionModel
{
	private String id;
	private String name;
	private String assetId;
	private TrackModel track;
	private Long onStationMillis;
	private Long offStationMillis;
	private Double speedMPH;
	private Double altitudeMeters;

	public String produceCzml(
			final TrackCzmlProperties properties ) {
		final String id = UUID.randomUUID()
				.toString();

		Packet packet = Packet.create()
				.id(id)
				.name("Mission " + name + " Asset " + assetId);

		if ((onStationMillis != null) && (offStationMillis != null)) {
			final Interval packetInterval = new Interval(
					DateTimeFactory.fromMillis(onStationMillis),
					DateTimeFactory.fromMillis(offStationMillis));

			packet = packet.availability(	packetInterval.getStart(),
											packetInterval.getEnd());
		}

		final List<String> trackCzml = new ArrayList<>();

		if (track != null) {
			trackCzml.add(track.produceCzml(id,
											properties,
											onStationMillis,
											offStationMillis));
		}

		trackCzml.add(	0,
						packet.writeString());

		return trackCzml.stream()
				.collect(Collectors.joining(","));
	}
}
