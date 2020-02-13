package com.maxar.cesium.czmlwriter.types;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import com.maxar.cesium.czmlwriter.CesiumLanguageWriterUtils;

import cesiumlanguagewriter.JulianDate;
import lombok.Data;

@Data
public class TimeTaggedValue<A>
{
	final DateTime time;
	final A value;

	public JulianDate timeAsJulian() {
		return CesiumLanguageWriterUtils.joda2Julian(time);
	}

	public static <A> List<JulianDate> listAsJulianDates(
			final List<TimeTaggedValue<A>> tags ) {
		return tags.stream()
				.map(tag -> tag.timeAsJulian())
				.collect(Collectors.toList());
	}

	public static <A> List<A> listAsValues(
			final List<TimeTaggedValue<A>> tags ) {
		return tags.stream()
				.map(tag -> tag.getValue())
				.collect(Collectors.toList());
	}

	public static <A, R> List<R> listAsValues(
			final List<TimeTaggedValue<A>> tags,
			Function<A, ? extends R> valueMapper ) {
		return tags.stream()
				.map(tag -> tag.getValue())
				.map(valueMapper)
				.collect(Collectors.toList());
	}
}
