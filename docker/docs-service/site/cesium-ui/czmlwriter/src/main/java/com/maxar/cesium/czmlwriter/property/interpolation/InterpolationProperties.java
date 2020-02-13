package com.maxar.cesium.czmlwriter.property.interpolation;

import org.joda.time.Duration;

import com.maxar.cesium.czmlwriter.CesiumLanguageWriterUtils;

import cesiumlanguagewriter.CesiumExtrapolationType;
import cesiumlanguagewriter.CesiumInterpolationAlgorithm;
import cesiumlanguagewriter.advanced.CesiumInterpolatablePropertyWriter;

public interface InterpolationProperties<A extends CesiumInterpolatablePropertyWriter<?>>
{
	public static <A extends CesiumInterpolatablePropertyWriter<?>> InterpolationProperties<A> create() {
		return writer -> {};
	}

	public void write(
			A writer );

	default InterpolationProperties<A> merge(
			final InterpolationProperties<A> properties ) {
		return writer -> {
			write(writer);
			properties.write(writer);
		};
	}

	default public InterpolationProperties<A> interpolationAlgorithm(
			final CesiumInterpolationAlgorithm interpolationAlgorithm ) {
		return merge(writer -> writer.writeInterpolationAlgorithm(interpolationAlgorithm));
	}

	default public InterpolationProperties<A> interpolationDegree(
			final Integer interpolationDegree ) {
		return merge(writer -> writer.writeInterpolationDegree(interpolationDegree));
	}

	default public InterpolationProperties<A> forwardExtrapolationType(
			final CesiumExtrapolationType forwardExtrapolationType ) {
		return merge(writer -> writer.writeForwardExtrapolationType(forwardExtrapolationType));
	}

	default public InterpolationProperties<A> forwardExtrapolationDuration(
			final Duration forwardExtrapolationDuration ) {
		return merge(writer -> writer.writeForwardExtrapolationDuration(CesiumLanguageWriterUtils
				.joda2Duration(forwardExtrapolationDuration)));
	}

	default public InterpolationProperties<A> backwardExtrapolationType(
			final CesiumExtrapolationType backwardExtrapolationType ) {
		return merge(writer -> writer.writeBackwardExtrapolationType(backwardExtrapolationType));
	}

	default public InterpolationProperties<A> backwardExtrapolationDuration(
			final Duration backwardExtrapolationDuration ) {
		return merge(writer -> writer.writeBackwardExtrapolationDuration(CesiumLanguageWriterUtils
				.joda2Duration(backwardExtrapolationDuration)));
	}
}
