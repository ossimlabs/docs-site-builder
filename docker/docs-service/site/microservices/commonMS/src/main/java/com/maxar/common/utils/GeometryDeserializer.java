package com.maxar.common.utils;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.radiantblue.analytics.core.log.SourceLogger;

public class GeometryDeserializer extends
		JsonDeserializer<Geometry>
{
	private static Logger logger = SourceLogger
			.getLogger(
					new Object() {}.getClass().getEnclosingClass().getName());

	@Override
	public Geometry deserialize(
			final JsonParser p,
			final DeserializationContext ctxt )
			throws IOException,
			JsonProcessingException {

		final String text = p.getText();
		if ((text == null) || (text.length() <= 0)) {
			return null;
		}

		final WKTReader reader = new WKTReader();
		Geometry geom = null;
		try {
			geom = reader
					.read(
							text);
		}
		catch (final ParseException e) {
			logger
					.error(
							"Cannot parse WKT string: " + text);
			return null;
		}

		return geom;
	}

}
