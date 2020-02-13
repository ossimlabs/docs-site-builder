package com.maxar.workflow.service;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.asset.example.AssetModelExamples;
import com.maxar.asset.model.IdList;
import com.maxar.asset.model.NameList;
import com.maxar.asset.model.SpaceAssetModel;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testworkflowms.properties")
public class WorkflowAssetServiceTest
{
	private static final String EXAMPLE_SPACE_NAME = "WV01";

	private static final String EXAMPLE_SPACE_ID = "32060";

	private static final String EXAMPLE_SPACE_ASSET_XML = AssetModelExamples.SPACE_WV01_XML;

	private static final String EXAMPLE_SPACE_ASSET_SENSOR_TYPE = "EO";

	@Autowired
	private WorkflowAssetService workflowAssetService;

	@MockBean
	private ApiService apiService;

	@Test
	public void testGetSpaceAssetNames() {
		final NameList nameList = new NameList();
		nameList.setNames(Collections.singletonList(EXAMPLE_SPACE_NAME));

		Mockito.when(apiService.getSpaceAssetNames())
				.thenReturn(nameList);

		final NameList result = workflowAssetService.getSpaceAssetNames();

		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getNames());
		Assert.assertEquals(1,
							result.getNames()
									.size());
		Assert.assertEquals(EXAMPLE_SPACE_NAME,
							result.getNames()
									.get(0));
	}

	@Test
	public void testGetSpaceNamesAssetEmpty() {
		final NameList nameList = new NameList();
		nameList.setNames(Collections.emptyList());

		Mockito.when(apiService.getSpaceAssetNames())
				.thenReturn(nameList);

		final NameList result = workflowAssetService.getSpaceAssetNames();

		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getNames());
		Assert.assertTrue(result.getNames()
				.isEmpty());
	}

	@Test
	public void testGetSpaceAssetsIDs() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(EXAMPLE_SPACE_ID));

		Mockito.when(apiService.getSpaceAssetsIDs())
				.thenReturn(idList);

		final IdList result = workflowAssetService.getSpaceAssetsIDs();

		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getIds());
		Assert.assertEquals(1,
							result.getIds()
									.size());
		Assert.assertEquals(EXAMPLE_SPACE_ID,
							result.getIds()
									.get(0));
	}

	@Test
	public void testGetSpaceAssetsIDsEmpty() {
		final IdList idList = new IdList();
		idList.setIds(Collections.emptyList());

		Mockito.when(apiService.getSpaceAssetsIDs())
				.thenReturn(idList);

		final IdList result = workflowAssetService.getSpaceAssetsIDs();

		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getIds());
		Assert.assertTrue(result.getIds()
				.isEmpty());
	}

	@Test
	public void testGetSpaceAssetModelWithID() {
		final SpaceAssetModel spaceAssetModel = new SpaceAssetModel();
		spaceAssetModel.setModelXml(EXAMPLE_SPACE_ASSET_XML);

		Mockito.when(apiService.getSpaceAssetModel(Mockito.eq(EXAMPLE_SPACE_ID)))
				.thenReturn(spaceAssetModel);

		final SpaceAssetModel result = workflowAssetService.getSpaceAssetModelWithID(EXAMPLE_SPACE_ID);

		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getModelXml());
		Assert.assertEquals(EXAMPLE_SPACE_ASSET_XML,
							result.getModelXml());
	}

	@Test
	public void testGetSpaceAssetModelWithIDNoMatch() {
		Mockito.when(apiService.getSpaceAssetModel(Mockito.eq(EXAMPLE_SPACE_ID)))
				.thenReturn(null);

		final SpaceAssetModel result = workflowAssetService.getSpaceAssetModelWithID(EXAMPLE_SPACE_ID);

		Assert.assertNull(result);
	}

	@Test
	public void testGetSpaceSensorTypesWithNames() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(EXAMPLE_SPACE_ID));

		Mockito.when(apiService.getSpaceAssetsIDs())
				.thenReturn(idList);

		final SpaceAssetModel spaceAssetModel = new SpaceAssetModel();
		spaceAssetModel.setModelXml(EXAMPLE_SPACE_ASSET_XML);

		Mockito.when(apiService.getSpaceAssetModel(Mockito.eq(EXAMPLE_SPACE_ID)))
				.thenReturn(spaceAssetModel);

		final SortedMap<String, List<String>> result = workflowAssetService
				.getSpaceSensorTypesWithNames(Collections.singletonList(EXAMPLE_SPACE_ASSET_SENSOR_TYPE));

		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(1,
							result.size());
		Assert.assertTrue(result.containsKey(EXAMPLE_SPACE_ASSET_SENSOR_TYPE));
		final List<String> assetNames = result.get(EXAMPLE_SPACE_ASSET_SENSOR_TYPE);
		Assert.assertNotNull(assetNames);
		Assert.assertEquals(1,
							assetNames.size());
		Assert.assertEquals(EXAMPLE_SPACE_NAME,
							assetNames.get(0));
	}

	@Test
	public void testGetSpaceSensorTypesWithNamesSensorTypesNull() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(EXAMPLE_SPACE_ID));

		Mockito.when(apiService.getSpaceAssetsIDs())
				.thenReturn(idList);

		final SpaceAssetModel spaceAssetModel = new SpaceAssetModel();
		spaceAssetModel.setModelXml(EXAMPLE_SPACE_ASSET_XML);

		Mockito.when(apiService.getSpaceAssetModel(Mockito.eq(EXAMPLE_SPACE_ID)))
				.thenReturn(spaceAssetModel);

		final SortedMap<String, List<String>> result = workflowAssetService.getSpaceSensorTypesWithNames(null);

		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(2,
							result.size());
		Assert.assertTrue(result.containsKey(EXAMPLE_SPACE_ASSET_SENSOR_TYPE));
		final List<String> assetNames = result.get(EXAMPLE_SPACE_ASSET_SENSOR_TYPE);
		Assert.assertNotNull(assetNames);
		Assert.assertEquals(1,
							assetNames.size());
		Assert.assertEquals(EXAMPLE_SPACE_NAME,
							assetNames.get(0));
	}

	@Test
	public void testGetSpaceSensorTypesWithNamesSensorTypesEmpty() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(EXAMPLE_SPACE_ID));

		Mockito.when(apiService.getSpaceAssetsIDs())
				.thenReturn(idList);

		final SpaceAssetModel spaceAssetModel = new SpaceAssetModel();
		spaceAssetModel.setModelXml(EXAMPLE_SPACE_ASSET_XML);

		Mockito.when(apiService.getSpaceAssetModel(Mockito.eq(EXAMPLE_SPACE_ID)))
				.thenReturn(spaceAssetModel);

		final SortedMap<String, List<String>> result = workflowAssetService
				.getSpaceSensorTypesWithNames(Collections.emptyList());

		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(2,
							result.size());
		Assert.assertTrue(result.containsKey(EXAMPLE_SPACE_ASSET_SENSOR_TYPE));
		final List<String> assetNames = result.get(EXAMPLE_SPACE_ASSET_SENSOR_TYPE);
		Assert.assertNotNull(assetNames);
		Assert.assertEquals(1,
							assetNames.size());
		Assert.assertEquals(EXAMPLE_SPACE_NAME,
							assetNames.get(0));
	}

	@Test
	public void testGetSpaceSensorTypesWithNamesNoSensorMatches() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(EXAMPLE_SPACE_ID));

		Mockito.when(apiService.getSpaceAssetsIDs())
				.thenReturn(idList);

		final SpaceAssetModel spaceAssetModel = new SpaceAssetModel();
		spaceAssetModel.setModelXml(EXAMPLE_SPACE_ASSET_XML);

		Mockito.when(apiService.getSpaceAssetModel(Mockito.eq(EXAMPLE_SPACE_ID)))
				.thenReturn(spaceAssetModel);

		final SortedMap<String, List<String>> result = workflowAssetService
				.getSpaceSensorTypesWithNames(Collections.singletonList("SAR"));

		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void testGetSpaceSensorTypesWithNamesInvalidXml() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(EXAMPLE_SPACE_ID));

		Mockito.when(apiService.getSpaceAssetsIDs())
				.thenReturn(idList);

		final SpaceAssetModel spaceAssetModel = new SpaceAssetModel();
		spaceAssetModel.setModelXml("invalid");

		Mockito.when(apiService.getSpaceAssetModel(Mockito.eq(EXAMPLE_SPACE_ID)))
				.thenReturn(spaceAssetModel);

		final SortedMap<String, List<String>> result = workflowAssetService
				.getSpaceSensorTypesWithNames(Collections.singletonList(EXAMPLE_SPACE_ASSET_SENSOR_TYPE));

		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void testGetSpaceSensorTypesWithIds() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(EXAMPLE_SPACE_ID));

		Mockito.when(apiService.getSpaceAssetsIDs())
				.thenReturn(idList);

		final SpaceAssetModel spaceAssetModel = new SpaceAssetModel();
		spaceAssetModel.setModelXml(EXAMPLE_SPACE_ASSET_XML);

		Mockito.when(apiService.getSpaceAssetModel(Mockito.eq(EXAMPLE_SPACE_ID)))
				.thenReturn(spaceAssetModel);

		final SortedMap<String, List<String>> result = workflowAssetService
				.getSpaceSensorTypesWithIds(Collections.singletonList(EXAMPLE_SPACE_ASSET_SENSOR_TYPE));

		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(1,
							result.size());
		Assert.assertTrue(result.containsKey(EXAMPLE_SPACE_ASSET_SENSOR_TYPE));
		final List<String> assetIds = result.get(EXAMPLE_SPACE_ASSET_SENSOR_TYPE);
		Assert.assertNotNull(assetIds);
		Assert.assertEquals(1,
							assetIds.size());
		Assert.assertEquals(EXAMPLE_SPACE_ID,
							assetIds.get(0));
	}

	@Test
	public void testGetSpaceSensorTypesWithIdsSensorTypesNull() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(EXAMPLE_SPACE_ID));

		Mockito.when(apiService.getSpaceAssetsIDs())
				.thenReturn(idList);

		final SpaceAssetModel spaceAssetModel = new SpaceAssetModel();
		spaceAssetModel.setModelXml(EXAMPLE_SPACE_ASSET_XML);

		Mockito.when(apiService.getSpaceAssetModel(Mockito.eq(EXAMPLE_SPACE_ID)))
				.thenReturn(spaceAssetModel);

		final SortedMap<String, List<String>> result = workflowAssetService.getSpaceSensorTypesWithIds(null);

		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(2,
							result.size());
		Assert.assertTrue(result.containsKey(EXAMPLE_SPACE_ASSET_SENSOR_TYPE));
		final List<String> assetIds = result.get(EXAMPLE_SPACE_ASSET_SENSOR_TYPE);
		Assert.assertNotNull(assetIds);
		Assert.assertEquals(1,
							assetIds.size());
		Assert.assertEquals(EXAMPLE_SPACE_ID,
							assetIds.get(0));
	}

	@Test
	public void testGetSpaceSensorTypesWithIdsSensorTypesEmpty() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(EXAMPLE_SPACE_ID));

		Mockito.when(apiService.getSpaceAssetsIDs())
				.thenReturn(idList);

		final SpaceAssetModel spaceAssetModel = new SpaceAssetModel();
		spaceAssetModel.setModelXml(EXAMPLE_SPACE_ASSET_XML);

		Mockito.when(apiService.getSpaceAssetModel(Mockito.eq(EXAMPLE_SPACE_ID)))
				.thenReturn(spaceAssetModel);

		final SortedMap<String, List<String>> result = workflowAssetService
				.getSpaceSensorTypesWithIds(Collections.emptyList());

		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());
		Assert.assertEquals(2,
							result.size());
		Assert.assertTrue(result.containsKey(EXAMPLE_SPACE_ASSET_SENSOR_TYPE));
		final List<String> assetIds = result.get(EXAMPLE_SPACE_ASSET_SENSOR_TYPE);
		Assert.assertNotNull(assetIds);
		Assert.assertEquals(1,
							assetIds.size());
		Assert.assertEquals(EXAMPLE_SPACE_ID,
							assetIds.get(0));
	}

	@Test
	public void testGetSpaceSensorTypesWithIdsNoSensorMatches() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(EXAMPLE_SPACE_ID));

		Mockito.when(apiService.getSpaceAssetsIDs())
				.thenReturn(idList);

		final SpaceAssetModel spaceAssetModel = new SpaceAssetModel();
		spaceAssetModel.setModelXml(EXAMPLE_SPACE_ASSET_XML);

		Mockito.when(apiService.getSpaceAssetModel(Mockito.eq(EXAMPLE_SPACE_ID)))
				.thenReturn(spaceAssetModel);

		final SortedMap<String, List<String>> result = workflowAssetService
				.getSpaceSensorTypesWithIds(Collections.singletonList("NOT EO"));

		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void testGetSpaceSensorTypesWithIdsInvalidXml() {
		final IdList idList = new IdList();
		idList.setIds(Collections.singletonList(EXAMPLE_SPACE_ID));

		Mockito.when(apiService.getSpaceAssetsIDs())
				.thenReturn(idList);

		final SpaceAssetModel spaceAssetModel = new SpaceAssetModel();
		spaceAssetModel.setModelXml("invalid");

		Mockito.when(apiService.getSpaceAssetModel(Mockito.eq(EXAMPLE_SPACE_ID)))
				.thenReturn(spaceAssetModel);

		final SortedMap<String, List<String>> result = workflowAssetService
				.getSpaceSensorTypesWithIds(Collections.singletonList(EXAMPLE_SPACE_ASSET_SENSOR_TYPE));

		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());
	}
}