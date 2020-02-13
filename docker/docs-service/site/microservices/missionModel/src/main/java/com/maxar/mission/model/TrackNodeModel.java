package com.maxar.mission.model;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.maxar.common.utils.GeometryDeserializer;
import com.maxar.common.utils.GeometrySerializer;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.LatLonAlt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackNodeModel
{
	private int sequence;

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	private Point wayPoint;
	private Long offsetMillis;

	@Builder.Default
	private boolean required = false;

	public TimeTaggedValue<LatLonAlt> produceTimeTaggedValue(
			final Long referenceTime ) {
		final TimeTaggedValue<LatLonAlt> latLonAlt = new TimeTaggedValue<>(
				new DateTime(
						referenceTime + offsetMillis),
				new LatLonAlt(
						Angle.fromDegrees(wayPoint.getY()),
						Angle.fromDegrees(wayPoint.getX()),
						Length.Zero()));

		return latLonAlt;
	}
}
