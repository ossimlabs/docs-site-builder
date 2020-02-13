package com.maxar.cesium.czmlwriter;

import java.io.IOException;
import java.io.StringWriter;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cesiumlanguagewriter.CesiumOutputStream;
import cesiumlanguagewriter.Iso8601Format;
import cesiumlanguagewriter.PacketCesiumWriter;
import cesiumlanguagewriter.advanced.CesiumFormattingHelper;

public class PacketWriter extends
		PacketCesiumWriter
{
	public static String LAYER_CONTROL_PROPETY_NAME = "layerControl";
	public static String TIMELINE_CONTROL_PROPETY_NAME = "timelineControl";

	final StringWriter sw;

	public PacketWriter() {
		sw = new StringWriter();
		final CesiumOutputStream output = new CesiumOutputStream(
				sw);
		output.setPrettyFormatting(true);
		open(output);
	}

	public void writeProperty(
			final String property,
			final boolean value ) {
		getOutput().writePropertyName(property);
		getOutput().writeValue(value);
	}

	public void writeProperty(
			final String property,
			final String value ) {
		getOutput().writePropertyName(property);
		getOutput().writeValue(value);
	}

	public void writeProperty(
			final String property,
			final DateTime value ) {
		getOutput().writePropertyName(property);
		getOutput().writeValue(CesiumFormattingHelper.toIso8601(CesiumLanguageWriterUtils.joda2Julian(value),
																getOutput().getPrettyFormatting()
																		? Iso8601Format.EXTENDED
																		: Iso8601Format.COMPACT));
	}

	public void openProperty(
			final String property ) {
		getOutput().writePropertyName(property);
		getOutput().writeStartObject();
	}

	public void closeProperty() {
		getOutput().writeEndObject();
	}

	public PacketWriter openLayerControl() {
		openProperty(LAYER_CONTROL_PROPETY_NAME);
		return this;
	}

	public PacketWriter openTimelineControl() {
		openProperty(TIMELINE_CONTROL_PROPETY_NAME);
		return this;
	}

	public ObjectNode toJsonNode() {
		try {

			return (ObjectNode) new ObjectMapper().readTree(this.toString());
		}
		catch (final IOException e) {
			// We're gonna assume cesium writer is making us valid JSON
			return null;
		}
	}

	@Override
	public String toString() {
		return sw.toString();
	}
}
