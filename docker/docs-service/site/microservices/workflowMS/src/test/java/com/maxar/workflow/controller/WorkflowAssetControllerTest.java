package com.maxar.workflow.controller;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.asset.model.NameList;
import com.maxar.workflow.service.WorkflowAssetService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testworkflowms.properties")
public class WorkflowAssetControllerTest
{
	private static final String EXAMPLE_SPACE_NAME = "space0";

	private static final String SENSOR_TYPE_RADAR = "RADAR";
	private static final String SENSOR_TYPE_EO = "EO";
	private static final List<String> SPACE_ASSETS_NAMES = List.of(	"CSM1",
																	"CSM2");
	private static final List<String> SPACE_ASSETS_IDS = List.of(	"31598",
																	"32376");

	@Autowired
	private WorkflowAssetController workflowAssetController;

	@MockBean
	private WorkflowAssetService workflowAssetService;

	@Test
	public void testGetSpaceNames() {
		final NameList nameList = new NameList();
		nameList.setNames(Collections.singletonList(EXAMPLE_SPACE_NAME));

		Mockito.when(workflowAssetService.getSpaceAssetNames())
				.thenReturn(nameList);

		final ResponseEntity<NameList> response = workflowAssetController.getSpaceNames();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertNotNull(response.getBody()
				.getNames());
		Assert.assertEquals(1,
							response.getBody()
									.getNames()
									.size());
		Assert.assertEquals(EXAMPLE_SPACE_NAME,
							response.getBody()
									.getNames()
									.get(0));
	}

	@Test
	public void testGetSpaceNamesEmpty() {
		final NameList nameList = new NameList();
		nameList.setNames(Collections.emptyList());

		Mockito.when(workflowAssetService.getSpaceAssetNames())
				.thenReturn(nameList);

		final ResponseEntity<NameList> response = workflowAssetController.getSpaceNames();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertNotNull(response.getBody()
				.getNames());
		Assert.assertTrue(response.getBody()
				.getNames()
				.isEmpty());
	}

	@Test
	public void testGetSpaceSensorTypesWithNamesEmpty() {
		TreeMap<String, List<String>> sensorTypes = new TreeMap<>();
		final List<String> emptyTest = List.of("TEST");
		Mockito.when(workflowAssetService.getSpaceSensorTypesWithNames(emptyTest))
				.thenReturn(sensorTypes);

		final ResponseEntity<SortedMap<String, List<String>>> response = workflowAssetController
				.getSpaceSensorTypesNames(emptyTest);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}

	@Test
	public void testGetSpaceSensorTypesWithNamesAll() {
		TreeMap<String, List<String>> sensorTypes = new TreeMap<>();
		sensorTypes.put(SENSOR_TYPE_RADAR,
						SPACE_ASSETS_NAMES);
		sensorTypes.put(SENSOR_TYPE_EO,
						SPACE_ASSETS_NAMES);
		final List<String> emptyTest = List.of();
		Mockito.when(workflowAssetService.getSpaceSensorTypesWithNames(emptyTest))
				.thenReturn(sensorTypes);

		final ResponseEntity<SortedMap<String, List<String>>> response = workflowAssetController
				.getSpaceSensorTypesNames(emptyTest);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.containsKey(SENSOR_TYPE_RADAR));
		Assert.assertTrue(response.getBody()
				.containsKey(SENSOR_TYPE_EO));
		Assert.assertTrue(response.getBody()
				.containsValue(SPACE_ASSETS_NAMES));
	}

	@Test
	public void testGetSpaceSensorTypesWithNamesFiltered() {
		TreeMap<String, List<String>> sensorTypes = new TreeMap<>();
		sensorTypes.put(SENSOR_TYPE_RADAR,
						SPACE_ASSETS_NAMES);
		final List<String> filter = List.of(SENSOR_TYPE_RADAR);
		Mockito.when(workflowAssetService.getSpaceSensorTypesWithNames(filter))
				.thenReturn(sensorTypes);

		final ResponseEntity<SortedMap<String, List<String>>> response = workflowAssetController
				.getSpaceSensorTypesNames(filter);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.containsKey(SENSOR_TYPE_RADAR));
		Assert.assertTrue(response.getBody()
				.containsValue(SPACE_ASSETS_NAMES));
	}

	@Test
	public void testGetSpaceSensorTypesWithIdsEmpty() {
		TreeMap<String, List<String>> sensorTypes = new TreeMap<>();
		final List<String> emptyTest = List.of("TEST");
		Mockito.when(workflowAssetService.getSpaceSensorTypesWithIds(emptyTest))
				.thenReturn(sensorTypes);

		final ResponseEntity<SortedMap<String, List<String>>> response = workflowAssetController
				.getSpaceSensorTypesIds(emptyTest);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}

	@Test
	public void testGetSpaceSensorTypesWithIdsAll() {
		TreeMap<String, List<String>> sensorTypes = new TreeMap<>();
		sensorTypes.put(SENSOR_TYPE_RADAR,
						SPACE_ASSETS_IDS);
		sensorTypes.put(SENSOR_TYPE_EO,
						SPACE_ASSETS_IDS);
		final List<String> emptyTest = List.of();
		Mockito.when(workflowAssetService.getSpaceSensorTypesWithIds(emptyTest))
				.thenReturn(sensorTypes);

		final ResponseEntity<SortedMap<String, List<String>>> response = workflowAssetController
				.getSpaceSensorTypesIds(emptyTest);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.containsKey(SENSOR_TYPE_RADAR));
		Assert.assertTrue(response.getBody()
				.containsKey(SENSOR_TYPE_EO));
		Assert.assertTrue(response.getBody()
				.containsValue(SPACE_ASSETS_IDS));
	}

	@Test
	public void testGetSpaceSensorTypesWithIdsFiltered() {
		TreeMap<String, List<String>> sensorTypes = new TreeMap<>();
		sensorTypes.put(SENSOR_TYPE_RADAR,
						SPACE_ASSETS_IDS);
		final List<String> filter = List.of(SENSOR_TYPE_RADAR);
		Mockito.when(workflowAssetService.getSpaceSensorTypesWithIds(filter))
				.thenReturn(sensorTypes);

		final ResponseEntity<SortedMap<String, List<String>>> response = workflowAssetController
				.getSpaceSensorTypesIds(filter);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.containsKey(SENSOR_TYPE_RADAR));
		Assert.assertTrue(response.getBody()
				.containsValue(SPACE_ASSETS_IDS));
	}
}
