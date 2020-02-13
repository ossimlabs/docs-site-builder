package com.maxar.cop.dm.ingest.planning;

import java.util.Collections;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxar.asset.model.OpBeam;
import com.maxar.cop.dm.cesium.CesiumTreeNode;
import com.maxar.cop.dm.manager.CopDataManager;
import com.maxar.cop.dm.service.CopServiceClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-cop-dm-service.properties")
public class AirbornePlanIngesterTest
{
	@MockBean
	private CopServiceClient serviceClient;

	@Autowired
	private AirbornePlanIngester airbornePlanIngester;

	@MockBean
	private CopDataManager manager;

	private static final String AIRBORNE_ASSET_ID = "AIRBORNE_ID1";

	private static final String AIRBORNE_MISSION_ID = "MISSION_ID1";

	@Test
	public void testUpdateCzmlData() {
		Mockito.when(serviceClient.getAirborneMissionIDs(	ArgumentMatchers.eq(AIRBORNE_ASSET_ID),
															ArgumentMatchers.any(),
															ArgumentMatchers.any()))
				.thenReturn(Collections.singletonList(AIRBORNE_MISSION_ID));

		Mockito.doNothing()
				.when(serviceClient)
				.createParentNode((CesiumTreeNode) ArgumentMatchers.any());

		final CesiumTreeNode model = new CesiumTreeNode(
				"testId",
				"testName",
				"testParent");
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode node = mapper.convertValue(	model,
													JsonNode.class);
		Mockito.when(serviceClient.retrieveCzml(ArgumentMatchers.any()))
				.thenReturn(Collections.singletonList(node));

		final OpBeam beam = new OpBeam();

		Mockito.when(serviceClient.getAirborneOps(	ArgumentMatchers.eq(AIRBORNE_MISSION_ID),
													ArgumentMatchers.any(),
													ArgumentMatchers.any()))
				.thenReturn(Collections.singletonList(beam));

		Mockito.doNothing()
				.when(serviceClient)
				.postPackets(	ArgumentMatchers.any(),
								ArgumentMatchers.any());

		airbornePlanIngester.updateCzmlData(AIRBORNE_ASSET_ID,
											DateTime.now(),
											DateTime.now());

		Assert.assertTrue(true);
	}
}
