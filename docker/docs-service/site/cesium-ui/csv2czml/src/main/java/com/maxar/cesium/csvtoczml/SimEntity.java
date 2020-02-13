package com.maxar.cesium.csvtoczml;

import org.joda.time.DateTime;

import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.LatLonAlt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimEntity
{
	String id;
	String offsetTime;
	Double lat;
	Double lon;

	public TimeTaggedValue<LatLonAlt> asTimeTaggedLatLonAlt() {
		return new TimeTaggedValue<LatLonAlt>(
				DateTime.parse(offsetTime),
				new LatLonAlt(
						Angle.fromDegrees(lat),
						Angle.fromDegrees(lon),
						Length.Zero()));
	}
}
