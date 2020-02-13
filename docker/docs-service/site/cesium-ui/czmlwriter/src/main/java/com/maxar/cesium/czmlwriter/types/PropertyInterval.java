package com.maxar.cesium.czmlwriter.types;

import org.joda.time.Interval;

import com.maxar.cesium.czmlwriter.CesiumLanguageWriterUtils;

import cesiumlanguagewriter.JulianDate;
import lombok.Data;

@Data
public class PropertyInterval<A>
{
	final Interval interval;
	final A property;

	public JulianDate startAsJulian() {
		return CesiumLanguageWriterUtils.joda2Julian(interval.getStart());
	}

	public JulianDate endAsJulian() {
		return CesiumLanguageWriterUtils.joda2Julian(interval.getEnd());
	}
}
