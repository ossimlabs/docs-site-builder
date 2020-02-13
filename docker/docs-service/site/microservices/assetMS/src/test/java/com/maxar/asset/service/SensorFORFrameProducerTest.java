package com.maxar.asset.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;

import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.ephemeris.model.TLEModel;
import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.isr.core.model.asset.Satellite;
import com.radiantblue.analytics.mechanics.orbit.ThreadSafeSGP4Propagator;
import com.radiantblue.analytics.mechanics.orbit.elementproviders.TLEElementProvider;

public class SensorFORFrameProducerTest
{
	private Asset asset;

	private TLEModel tleModel;

	@Before
	public void setUp()
			throws IOException {
		tleModel = TLEModel.tleBuilder()
				.scn(32060)
				.type(EphemerisType.TLE)
				.epochMillis(1560460296793L)
				.description("WORLDVIEW-1 (WV-1)")
				.tleLineOne("1 32060U 07041A   19164.88306474  .00000453  00000-0  21379-4 0  9992")
				.tleLineTwo("2 32060  97.3900 284.5950 0001530 113.2584 246.8815 15.24382087652969")
				.build();

		try (final InputStream inputStream = this.getClass()
				.getResourceAsStream("/xml/WV01_full.xml")) {
			final String assetXml = new BufferedReader(
					new InputStreamReader(
							inputStream)).lines()
									.collect(Collectors.joining(System.lineSeparator()));

			final GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext(
					new ByteArrayResource(
							assetXml.getBytes(StandardCharsets.UTF_8)));

			asset = applicationContext.getBean(Satellite.class);
			applicationContext.close();
		}

		Assert.assertNotNull(asset);

		asset.setPropagator(new ThreadSafeSGP4Propagator(
				new TLEElementProvider(
						tleModel.getDescription(),
						tleModel.getTleLineOne(),
						tleModel.getTleLineTwo())));

		asset.init();

		Assert.assertNotNull(asset.getPayloads());
		Assert.assertEquals(2,
							asset.getPayloads()
									.size());
		Assert.assertNotNull(asset.getPayloads()
				.get(0));
		Assert.assertNotNull(asset.getPayloads()
				.get(1));
		Assert.assertNotNull(asset.getPayloads()
				.get(1)
				.sensors());
		Assert.assertEquals(1,
							asset.getPayloads()
									.get(1)
									.sensors()
									.size());
		Assert.assertNotNull(asset.getPayloads()
				.get(1)
				.sensors()
				.get(0));
	}

	@Test
	public void testGetFOR() {
		final SensorFORFrameProducer sensorFORFrameProducer = new SensorFORFrameProducer(
				asset.getPayloads()
						.get(1)
						.sensors()
						.get(0));

		final DateTime atTime = new DateTime(
				tleModel.getEpochMillis(),
				DateTimeZone.UTC);

		final GeodeticGeometry forGeometry = sensorFORFrameProducer.getFOR(atTime);

		Assert.assertNotNull(forGeometry);
	}

	@Test
	public void testGetName() {
		final SensorFORFrameProducer sensorFORFrameProducer = new SensorFORFrameProducer(
				asset.getPayloads()
						.get(1)
						.sensors()
						.get(0));

		Assert.assertEquals("WV01_ScanningSensor",
							sensorFORFrameProducer.getName());
	}

	@Test
	public void testGetSensorType() {
		final SensorFORFrameProducer sensorFORFrameProducer = new SensorFORFrameProducer(
				asset.getPayloads()
						.get(1)
						.sensors()
						.get(0));

		Assert.assertEquals("EO",
							sensorFORFrameProducer.getSensorType());
	}
}
