package com.maxar.cop.dm.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maxar.asset.model.IdList;
import com.maxar.asset.model.NameList;
import com.maxar.asset.model.OpBeam;
import com.maxar.cop.dm.manager.CopDataManager;
import com.maxar.mission.model.MissionModel;
import com.maxar.planning.model.image.CollectionWindowModel;
import com.maxar.planning.model.image.ImageFrameModel;
import com.maxar.planning.model.image.ImageOpModel;
import com.maxar.planning.model.tasking.TaskingModel;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-cop-dm-service.properties")
public class CopServiceClientTest
{
	@MockBean
	private RestTemplate restTemplate;

	@MockBean
	private CopDataManager manager;

	@Autowired
	private CopServiceClient serviceClient;

	@Value("${microservices.copdm.assetAllIdsUrl}")
	private String assetAllIdsUrl;

	@Value("${microservices.copdm.cesiumUiRequestUrl}")
	private String cesiumUiRequestUrl;

	private static final String SPACE_ASSET_ID = "SPACE_ID1";

	private static final String AIRBORNE_ASSET_ID = "AIRBORNE_ID1";

	private static final String AIRBORNE_ASSET_NAME = "AIRBORNE_NAME1";

	private static final String AIRBORNE_MISSION_ID = "MISSION_ID1";

	@Test
	public void testGetSpaceAssetIDs() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(SPACE_ASSET_ID));

		Mockito.when(restTemplate.getForEntity(	ArgumentMatchers.anyString(),
												ArgumentMatchers.eq(IdList.class)))
				.thenReturn(ResponseEntity.ok(idList));

		final List<String> spaceIds = serviceClient.getSpaceAssetIDs();

		Assert.assertNotNull(spaceIds);
		Assert.assertEquals(1,
							spaceIds.size());
		Assert.assertEquals(SPACE_ASSET_ID,
							spaceIds.get(0));
	}

	@Test
	public void testPostCzmlToUI() {
		Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(),
												ArgumentMatchers.any(),
												ArgumentMatchers.eq(Boolean.class)))
				.thenReturn(ResponseEntity.ok(Boolean.TRUE));

		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode request = mapper.createObjectNode();
		serviceClient.postCzmlToUI(request);

		Assert.assertTrue(true);
	}

	@Test
	public void testGetAirborneAssetNames() {
		final NameList nameList = new NameList();
		nameList.setNames(Collections.singletonList(AIRBORNE_ASSET_NAME));

		Mockito.when(restTemplate.getForEntity(	ArgumentMatchers.anyString(),
												ArgumentMatchers.eq(NameList.class)))
				.thenReturn(ResponseEntity.ok(nameList));

		final List<String> names = serviceClient.getAirborneAssetNames();

		Assert.assertNotNull(names);
		Assert.assertEquals(1,
							names.size());
		Assert.assertEquals(AIRBORNE_ASSET_NAME,
							names.get(0));
	}

	@Test
	public void testGetAirborneAssetIDs() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(AIRBORNE_ASSET_ID));

		Mockito.when(restTemplate.getForEntity(	ArgumentMatchers.anyString(),
												ArgumentMatchers.eq(IdList.class)))
				.thenReturn(ResponseEntity.ok(idList));

		final List<String> ids = serviceClient.getAirborneAssetIDs();

		Assert.assertNotNull(ids);
		Assert.assertEquals(1,
							ids.size());
		Assert.assertEquals(AIRBORNE_ASSET_ID,
							ids.get(0));
	}

	@Test
	public void testGetAirborneMissionIds() {
		final MissionModel model = MissionModel.builder()
				.id(AIRBORNE_MISSION_ID)
				.build();

		Mockito.when(restTemplate.exchange(	ArgumentMatchers.any(URI.class),
											ArgumentMatchers.eq(HttpMethod.GET),
											ArgumentMatchers.isNull(),
											(ParameterizedTypeReference<List<MissionModel>>) ArgumentMatchers.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(model)));

		final List<String> ids = serviceClient.getAirborneMissionIDs(	AIRBORNE_ASSET_ID,
																		DateTime.now(),
																		DateTime.now());

		Assert.assertNotNull(ids);
		Assert.assertEquals(1,
							ids.size());
		Assert.assertEquals(AIRBORNE_MISSION_ID,
							ids.get(0));
	}

	@Test
	public void testGetAirborneOps() {
		final TaskingModel model = TaskingModel.builder()
				.totTimeMillis(DateTime.now()
						.getMillis())
				.build();

		Mockito.when(restTemplate.exchange(	ArgumentMatchers.any(URI.class),
											ArgumentMatchers.eq(HttpMethod.GET),
											ArgumentMatchers.isNull(),
											(ParameterizedTypeReference<List<TaskingModel>>) ArgumentMatchers.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(model)));

		final List<OpBeam> beams = serviceClient.getAirborneOps(AIRBORNE_MISSION_ID,
																DateTime.now(),
																DateTime.now());

		Assert.assertNotNull(beams);
		Assert.assertEquals(1,
							beams.size());
	}

	@Test
	public void testGetCws() {
		final CollectionWindowModel model = CollectionWindowModel.builder()
				.assetName(SPACE_ASSET_ID)
				.build();

		Mockito.when(restTemplate.exchange(	ArgumentMatchers.any(URI.class),
											ArgumentMatchers.eq(HttpMethod.GET),
											ArgumentMatchers.isNull(),
											(ParameterizedTypeReference<List<CollectionWindowModel>>) ArgumentMatchers
													.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(model)));

		final List<CollectionWindowModel> cws = serviceClient.getCws(	SPACE_ASSET_ID,
																		DateTime.now(),
																		DateTime.now());

		Assert.assertNotNull(cws);
		Assert.assertEquals(1,
							cws.size());
		Assert.assertEquals(SPACE_ASSET_ID,
							cws.get(0)
									.getAssetName());
	}

	@Test
	public void testGetSpaceOps() {
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
				.imageOps(Collections.singleton(op))
				.build();

		Mockito.when(restTemplate.exchange(	ArgumentMatchers.any(URI.class),
											ArgumentMatchers.eq(HttpMethod.GET),
											ArgumentMatchers.isNull(),
											(ParameterizedTypeReference<List<CollectionWindowModel>>) ArgumentMatchers
													.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(model)));

		final List<OpBeam> beams = serviceClient.getSpaceOps(	SPACE_ASSET_ID,
																DateTime.now(),
																DateTime.now());

		Assert.assertNotNull(beams);
		Assert.assertEquals(1,
							beams.size());
	}

	@Test
	public void testCreateParentNode() {
		Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(),
												ArgumentMatchers.any(),
												ArgumentMatchers.eq(Boolean.class)))
				.thenReturn(ResponseEntity.ok(Boolean.TRUE));

		serviceClient.createParentNode("EXAMPLE");

		Assert.assertTrue(true);
	}

	@Test
	public void testPostPackets() {
		Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(),
												ArgumentMatchers.any(),
												ArgumentMatchers.eq(Boolean.class)))
				.thenReturn(ResponseEntity.ok(Boolean.TRUE));

		final List<JsonNode> packets = new ArrayList<>();
		serviceClient.postPackets(	packets,
									"parent");

		Assert.assertTrue(true);
	}

	@Test
	public void testRetrieveCzml() {
		final ImageFrameModel model = new ImageFrameModel();
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode node = mapper.convertValue(	model,
													JsonNode.class);

		Mockito.when(restTemplate.exchange(	ArgumentMatchers.any(URI.class),
											ArgumentMatchers.eq(HttpMethod.POST),
											(HttpEntity<JsonNode>) ArgumentMatchers.any(),
											(ParameterizedTypeReference<List<JsonNode>>) ArgumentMatchers.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(node)));

		final ObjectNode request = mapper.createObjectNode();
		// just need a random jsonnode for these request fields
		request.set("body",
					node);
		request.set("url",
					node);

		final List<JsonNode> nodes = serviceClient.retrieveCzml(request);

		Assert.assertNotNull(nodes);
		Assert.assertEquals(1,
							nodes.size());
	}
}
