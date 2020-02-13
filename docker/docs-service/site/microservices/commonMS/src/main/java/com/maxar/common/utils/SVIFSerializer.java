package com.maxar.common.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.radiantblue.analytics.core.Vector3D;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

public class SVIFSerializer extends
		JsonSerializer<StateVectorsInFrame>
{

	@Override
	public void serialize(
			final StateVectorsInFrame svif,
			final JsonGenerator jgen,
			final SerializerProvider serializerProvider )
			throws IOException {
		jgen.writeStartObject();
		jgen.writeStringField(	"atTime",
								svif.atTime()
										.toString());
		writeVector3DField(	svif.getPosition(),
							jgen,
							"position");
		writeVector3DField(	svif.getVelocity(),
							jgen,
							"velocity");
		jgen.writeEndObject();
	}

	private void writeVector3DField(
			final Vector3D vec,
			final JsonGenerator jgen,
			final String tagName )
			throws IOException {
		jgen.writeObjectFieldStart(tagName);
		jgen.writeNumberField(	"x",
								vec.x());
		jgen.writeNumberField(	"y",
								vec.y());
		jgen.writeNumberField(	"z",
								vec.z());
		jgen.writeEndObject();
	}

}
