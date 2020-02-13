package com.maxar.cop.dm.ingest.planning;

import java.util.Collections;
import java.util.List;

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
import com.maxar.cop.dm.cesium.CesiumTreeNode;
import com.maxar.cop.dm.manager.CopDataManager;
import com.maxar.cop.dm.service.CopServiceClient;
import com.maxar.planning.model.image.CollectionWindowModel;
import com.maxar.planning.model.image.ImageFrameModel;
import com.maxar.planning.model.image.ImageOpModel;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-cop-dm-service.properties")
public class SpacePlanIngesterTest
{
	@MockBean
	private CopServiceClient serviceClient;

	@Autowired
	private SpacePlanIngester spacePlanIngester;

	@MockBean
	private CopDataManager manager;

	private static final String SPACE_ASSET_ID = "SPACE_ID1";

	@Test
	public void testUpdateCzmlData() {
		final List<CollectionWindowModel> cws = initializeTestCws();

		Mockito.when(serviceClient.getCws(	ArgumentMatchers.eq(SPACE_ASSET_ID),
											ArgumentMatchers.any(),
											ArgumentMatchers.any()))
				.thenReturn(cws);

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

		Mockito.doNothing()
				.when(serviceClient)
				.postPackets(	ArgumentMatchers.any(),
								ArgumentMatchers.any());

		spacePlanIngester.updateCzmlData(	SPACE_ASSET_ID,
											DateTime.now(),
											DateTime.now());

		Assert.assertTrue(true);
	}

	private List<CollectionWindowModel> initializeTestCws() {
		final ImageFrameModel frame = ImageFrameModel.builder()
				.build();

		final ImageOpModel op = ImageOpModel.builder()
				.opStartTimeMillis(DateTime.now()
						.getMillis())
				.opEndTimeMillis(DateTime.now()
						.getMillis())
				.imageFrames(Collections.singleton(frame))
				.build();

		final CollectionWindowModel model = CollectionWindowModel.builder()
				.assetName(SPACE_ASSET_ID)
				.startMillis(DateTime.now()
						.getMillis())
				.endMillis(DateTime.now()
						.getMillis())
				.imageOps(Collections.singleton(op))
				.build();

		return Collections.singletonList(model);
	}
}
