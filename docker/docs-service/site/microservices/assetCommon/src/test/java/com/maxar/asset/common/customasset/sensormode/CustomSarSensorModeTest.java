package com.maxar.asset.common.customasset.sensormode;

import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.maxar.asset.common.utils.PhysicalConstants;
import com.radiantblue.analytics.aerospace.geometry.PointToPointGeometry;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPointLLA;
import com.radiantblue.analytics.isr.core.component.accgen.Access;
import com.radiantblue.analytics.isr.core.component.accgen.IAccess;
import com.radiantblue.analytics.isr.core.component.accgen.IAccessRequest;
import com.radiantblue.analytics.isr.core.component.accgen.request.SimpleAccessRequest;
import com.radiantblue.analytics.isr.core.model.asset.Asset;

public class CustomSarSensorModeTest
{
	@Before
	public void setUp() {
		final GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
		applicationContext.load("/RS02.xml");
		applicationContext.refresh();
		// Get the asset and mode for testing
		asset = (Asset) applicationContext.getBean("RS02");
		asset.init();
		sensorMode = (CustomSarSensorMode) applicationContext.getBean("RS02_Custom_SAR_Mode");
		sensorMode.init();
		applicationContext.close();

		tgt = new GeodeticPointLLA(
				Angle.fromDegrees(42.0),
				Angle.fromDegrees(-55.0),
				Length.fromNmi(2.0));
	}

	@Test
	public void testDwell() {
		final Double quality = 5.0;
		final IAccessRequest accReq = new SimpleAccessRequest(
				startTime,
				endTime,
				asset,
				tgt,
				null,
				null);
		final IAccess access = new Access(
				startTime,
				endTime,
				accReq);
		final Double dwellSec = sensorMode.getDwellTimeInSeconds(	access,
																	quality,
																	startTime);
		System.out.println("dwellSec=" + dwellSec);
		Assert.assertTrue(dwellSec != null);
		Assert.assertEquals(5.0,
							dwellSec,
							1e-9);
	}

	@Test
	public void testQuality() {
		final PointToPointGeometry geom = new PointToPointGeometry(
				asset,
				tgt,
				startTime);
		final List<Double> qualities = sensorMode.getQualities(geom)
				.stream()
				.map(Double.class::cast)
				.collect(Collectors.toList());
		System.out.println("qualities[0]=" + qualities.get(0));
		Assert.assertTrue(qualities.size() == 1);
		Assert.assertEquals(10.0,
							qualities.get(0),
							1e-9);
	}

	@Test
	public void testMnvr() {
		final Double tgtAngleRads = 0.523599; // 30deg
		final IAccessRequest accReq = new SimpleAccessRequest(
				startTime,
				endTime,
				asset,
				tgt,
				null,
				null);
		final IAccess access = new Access(
				startTime,
				endTime,
				accReq);
		final Double mnvrTimeSec = sensorMode.getManeuverTimeInSeconds(	access,
																		tgtAngleRads);
		System.out.println("mnvrTimeSec=" + mnvrTimeSec);
		Assert.assertEquals(mnvrTimeSec,
							sensorMode.getMinTimeBetweenDwells(),
							1e-9);
	}

	@Test
	public void testConstants() {
		System.out.println("SPEED_OF_LIGHT: " + PhysicalConstants.SPEED_OF_LIGHT + " (m/s)");
		System.out.println("BOLTZMANN: " + PhysicalConstants.BOLTZMANN + " (J/degK)");
		System.out.println("K: " + PhysicalConstants.K + " (BOLTZMANN in dB => dBW/K/Hz)");
	}

	private Asset asset;
	private GeodeticPointLLA tgt;
	private CustomSarSensorMode sensorMode;
	private final DateTime startTime = DateTime.parse("2019-01-01T00:00:00Z")
			.withZone(DateTimeZone.UTC);
	private final DateTime endTime = DateTime.parse("2019-01-01T00:00:30Z")
			.withZone(DateTimeZone.UTC);
}
