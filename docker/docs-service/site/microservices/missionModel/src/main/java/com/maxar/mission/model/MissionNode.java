package com.maxar.mission.model;

import org.joda.time.Duration;

import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MissionNode implements
		Comparable<MissionNode>
{
	private int sequence;
	private GeodeticPoint wayPoint;
	private Duration offset;

	public MissionNode(
			TrackNodeModel model,
			Length altitude ) {
		sequence = model.getSequence();
		wayPoint = GeodeticPoint
				.fromLatLonAlt(
						Angle
								.fromDegrees(
										model.getWayPoint().getY()),
						Angle
								.fromDegrees(
										model.getWayPoint().getX()),
						altitude);
		offset = Duration
				.millis(
						model.getOffsetMillis());
	}

	@Override
	public int compareTo(
			MissionNode node ) {
		return sequence - node.sequence;
	}
}
