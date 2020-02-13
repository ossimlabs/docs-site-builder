package com.maxar.common.utils;

import java.io.IOException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class GeometrySerializer extends
		JsonSerializer<Geometry>
{
	@Override
	public void serialize(
			final Geometry geom,
			final JsonGenerator jsonGenerator,
			final SerializerProvider serializerProvider )
			throws IOException,
			JsonProcessingException {
		final WKTWriter writer = new WKTWriter();

		jsonGenerator
				.writeString(
						writer
								.writeFormatted(
										geom));
	}
}
