package com.maxar.asset.service;

import org.joda.time.DateTime;

import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;
import com.radiantblue.analytics.isr.core.model.sensor.ISensor;

public class SensorFORFrameProducer implements
		FORFrameProducer
{
	private final ISensor sensor;

	public SensorFORFrameProducer(
			final ISensor sensor ) {
		this.sensor = sensor;
	}

	@Override
	public GeodeticGeometry getFOR(
			final DateTime atTime ) {
		return sensor.getFOR(atTime);
	}

	@Override
	public String getName() {
		return sensor.getName();
	}

	@Override
	public String getSensorType() {
		return sensor.getSensorType();
	}
}
