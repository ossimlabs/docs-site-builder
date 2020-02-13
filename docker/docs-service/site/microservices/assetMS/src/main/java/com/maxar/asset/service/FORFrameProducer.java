package com.maxar.asset.service;

import org.joda.time.DateTime;

import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;

/*
 * Created as a wrapper for rb-analytics asset, payload, and sensor since they do not share a common
 * getFOR interface
 */
public interface FORFrameProducer
{
	GeodeticGeometry getFOR(
			DateTime atTime );

	String getName();

	String getSensorType();
}
