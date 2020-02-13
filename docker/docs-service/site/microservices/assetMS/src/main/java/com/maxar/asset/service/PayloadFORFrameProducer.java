package com.maxar.asset.service;

import org.joda.time.DateTime;

import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;
import com.radiantblue.analytics.isr.core.model.payload.IPayload;

public class PayloadFORFrameProducer implements
		FORFrameProducer
{

	private final IPayload payload;

	public PayloadFORFrameProducer(
			final IPayload payload ) {
		this.payload = payload;
	}

	@Override
	public GeodeticGeometry getFOR(
			final DateTime atTime ) {
		return payload.getFOR(atTime);
	}

	@Override
	public String getName() {
		return payload.getName();
	}

	@Override
	public String getSensorType() {
		return payload.getPayloadType();
	}
}
