package com.maxar.cop.dm.ingest.track;

import java.util.Collections;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maxar.cop.dm.cesium.CesiumTreeNode;
import com.maxar.cop.dm.manager.CopDataManager;
import com.maxar.cop.dm.service.CopServiceClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-cop-dm-service.properties")
public class TrackIngesterTest
{
	@MockBean
	private CopServiceClient serviceClient;

	@Autowired
	private TrackIngester trackIngester;

	@MockBean
	private CopDataManager manager;

	private static final String AIRBORNE_ASSET_ID = "AIRBORNE_ID1";

	@Test
	public void testUpdateCzmlData() {
		Mockito.doNothing()
				.when(serviceClient)
				.postCzmlToUI((ObjectNode) ArgumentMatchers.any());

		final CesiumTreeNode model = new CesiumTreeNode(
				"testId",
				"testName",
				"testParent");
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode node = mapper.convertValue(	model,
													JsonNode.class);
		Mockito.when(serviceClient.retrieveCzml(ArgumentMatchers.any()))
				.thenReturn(Collections.singletonList(node));

		Mockito.doNothing()
				.when(serviceClient)
				.postPackets(	ArgumentMatchers.any(),
								ArgumentMatchers.any());

		trackIngester.updateCzmlData(	AIRBORNE_ASSET_ID,
										DateTime.now(),
										DateTime.now());

		Assert.assertTrue(true);
	}

	@Test
	public void testCleanupStaleDataWithinPackets() {
		Mockito.doNothing()
				.when(serviceClient)
				.postPackets(	ArgumentMatchers.any(),
								ArgumentMatchers.any());

		final DateTime startTime = DateTime.now();
		final DateTime stopTime = startTime.plus(5000l);
		final Interval deleteInterval = new Interval(
				startTime,
				stopTime);

		final Set<String> smearPacketIds = Collections.singleton("testSmearPacketId");
		try {
			FieldSetter.setField(	trackIngester,
									trackIngester.getClass()
											.getSuperclass()
											.getDeclaredField("smearPacketIds"),
									smearPacketIds);
		}
		catch (NoSuchFieldException | SecurityException e) {
			Assert.fail(e.getMessage());
		}

		trackIngester.cleanupStaleDataWithinPackets(Collections.singletonList(AIRBORNE_ASSET_ID),
													deleteInterval);
	}
}
