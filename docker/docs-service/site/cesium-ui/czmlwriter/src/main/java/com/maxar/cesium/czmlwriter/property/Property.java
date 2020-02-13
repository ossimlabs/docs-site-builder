package com.maxar.cesium.czmlwriter.property;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.Interval;

import com.maxar.cesium.czmlwriter.property.deletable.DeletableProperty;
import com.maxar.cesium.czmlwriter.types.PropertyInterval;

import cesiumlanguagewriter.CesiumIntervalListWriter;
import cesiumlanguagewriter.advanced.CesiumPropertyWriter;
import cesiumlanguagewriter.advanced.ICesiumDeletablePropertyWriter;
import cesiumlanguagewriter.advanced.ICesiumPropertyWriter;

public interface Property<A extends ICesiumPropertyWriter>
{
	public void write(
			A writer );

	public static <A extends CesiumPropertyWriter<A>, B extends Property<A>> Property<A> interval(
			final List<PropertyInterval<B>> intervals ) {
		return writer -> {
			if (intervals.size() == 0) {
				return;
			}
			else if (intervals.size() == 1) {
				writeInterval(	writer,
								intervals.get(0));
				return;
			}
			else {
				final CesiumIntervalListWriter<?> intervalListWriter = writer.openMultipleIntervals();
				intervals.forEach(intervalValue -> {
					writeInterval(	writer,
									intervalValue);
				});
				intervalListWriter.close();
			}
		};
	}

	private static <A extends CesiumPropertyWriter<A>, B extends Property<A>> void writeInterval(
			final A writer,
			final PropertyInterval<B> intervalValue ) {
		final A intervalWriter = writer.openInterval(	intervalValue.startAsJulian(),
														intervalValue.endAsJulian());
		intervalValue.getProperty()
				.write(intervalWriter);
		intervalWriter.close();
	}

	public static <A extends CesiumPropertyWriter<A> & ICesiumDeletablePropertyWriter> Property<A> deleteInterval(
			final Interval interval ) {
		return deleteInterval(Collections.singletonList(interval));
	}

	public static <A extends CesiumPropertyWriter<A> & ICesiumDeletablePropertyWriter> Property<A> deleteInterval(
			final List<Interval> intervals ) {

		return interval(intervals.stream()
				.map(interval -> new PropertyInterval<Property<A>>(
						interval,
						DeletableProperty.<A> createDelete()))
				.collect(Collectors.toList()));
	}

	public static <A extends CesiumPropertyWriter<A>> void writeAndClose(
			final A writer,
			final Property<A> property ) {
		property.write(writer);
		writer.close();
	}
}
