package com.maxar.access.csv;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.access.model.Access;
import com.maxar.access.model.UntrimmedAccess;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testaccessms.properties")
public class AccessCsvTypeHandlerTest
{
	@Autowired
	private AccessCsvTypeHandler accessCsvTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(accessCsvTypeHandler.canHandle(UntrimmedAccess.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(accessCsvTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHeaders() {
		Assert.assertEquals("START, TCA, END, ASSET ID, SENSOR MODE, FAILURE REASON, REV, PASS, PROPAGATOR, "
				+ "GEOMETRY, TRIMMED START, TRIMMED TCA, TRIMMED END",
							accessCsvTypeHandler.headers());
	}

	@Test
	public void testHandle()
			throws ParseException {
		final WKTReader wktReader = new WKTReader();
		final Geometry geometry = wktReader.read("POINT(0.0 0.0)");

		final Access access = new Access();
		access.setStartTimeISO8601("");
		access.setEndTimeISO8601("");
		access.setTcaTimeISO8601("");

		final UntrimmedAccess untrimmedAccess = new UntrimmedAccess();
		untrimmedAccess.setStartTimeISO8601("");
		untrimmedAccess.setEndTimeISO8601("");
		untrimmedAccess.setTcaTimeISO8601("");
		untrimmedAccess.setAssetID("asset0");
		untrimmedAccess.setSensorMode("sensormode0");
		untrimmedAccess.setFailureReason(null);
		untrimmedAccess.setRev(0);
		untrimmedAccess.setPass(0);
		untrimmedAccess.setPropagatorType("J2");
		untrimmedAccess.setGeometry(geometry);
		untrimmedAccess.setTrimmedAccesses(Collections.singletonList(access));

		final List<String> csvList = accessCsvTypeHandler.handle(untrimmedAccess);

		Assert.assertNotNull(csvList);
		Assert.assertEquals(2,
							csvList.size());
		Assert.assertEquals(", , , asset0, sensormode0, , 0, 0, J2, POINT (0 0)",
							csvList.get(0));
		Assert.assertEquals(", , , , , , , , , , , , ",
							csvList.get(1));
	}
}
