package com.maxar.opgen.czml;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.opgen.model.Op;
import com.maxar.opgen.model.OpBeam;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testopgen.properties")
public class OpCzmlTypeHandlerTest
{
	@Autowired
	private OpCzmlTypeHandler opCzmlTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(opCzmlTypeHandler.canHandle(Op.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(opCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertFalse(opCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandle() {
		final Op op = new Op();
		op.setAssetName("TEST");
		op.setSensorType("EO");
		final DateTime start = DateTime.parse("2020-01-01T03:00:00.000Z");
		final DateTime end = start.plusSeconds(5);
		op.setStartTime(start);
		op.setEndTime(end);
		op.setValid(true);
		final OpBeam beam = new OpBeam();
		beam.setStartTime(start);
		beam.setEndTime(end);
		beam.setGeometryWkt("POLYGON((1.0 2.0, 2.0 2.0, 2.0 1.0, 1.0 1.0, 1.0 2.0))");
		op.setBeams(Collections.singletonList(beam));

		final List<String> czmls = opCzmlTypeHandler.handle(op);

		Assert.assertNotNull(czmls);
		Assert.assertEquals(1,
							czmls.size());
		Assert.assertTrue(czmls.get(0)
				.contains("\"name\":\"ModeOp 2020-01-01T03:00:00.000Z\""));
		Assert.assertTrue(czmls.get(0)
				.contains("\"group\":\"TEST:EO Ops\""));
	}
}
