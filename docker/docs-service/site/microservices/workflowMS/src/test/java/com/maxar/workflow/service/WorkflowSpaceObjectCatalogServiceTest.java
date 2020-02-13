package com.maxar.workflow.service;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.spaceobjectcatalog.model.SpaceObject;
import com.maxar.workflow.model.EphemeridesRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testworkflowms.properties")
public class WorkflowSpaceObjectCatalogServiceTest
{
	@Autowired
	private WorkflowSpaceObjectCatalogService workflowSpaceObjectCatalogService;

	@MockBean
	private ApiService apiService;

	@Test
	public void testGetScnForAssetsByName() {

		final EphemeridesRequest ephemeridesRequest = new EphemeridesRequest();
		ephemeridesRequest.setAssetNames(Collections.singletonList("asset01"));
		ephemeridesRequest.setCount(1);
		ephemeridesRequest.setPage(1);
		Mockito.doReturn(1)
				.when(apiService)
				.getSpaceAssetIdByName("asset01");

		final List<Integer> results = workflowSpaceObjectCatalogService.getScnForAssetsByName(ephemeridesRequest);

		Assert.assertNotNull(results);
		Assert.assertNotNull(results.get(0));
		Assert.assertEquals((Integer) 1,
							results.get(0));

	}

	@Test
	public void testGetScnForAssetsByNameEmpty() {

		final EphemeridesRequest ephemeridesRequest = new EphemeridesRequest();
		ephemeridesRequest.setAssetNames(Collections.emptyList());
		ephemeridesRequest.setCount(1);
		ephemeridesRequest.setPage(1);
		Mockito.doReturn(1)
				.when(apiService)
				.getSpaceAssetIdByName("asset01");

		final List<Integer> results = workflowSpaceObjectCatalogService.getScnForAssetsByName(ephemeridesRequest);

		Assert.assertNotNull(results);
		Assert.assertTrue(results.isEmpty());

	}

	@Test
	public void testGetTleByAssetName() {

		EphemeridesRequest ephemeridesRequest = new EphemeridesRequest();
		ephemeridesRequest.setAssetNames(Collections.singletonList("asset01"));
		ephemeridesRequest.setCount(1);
		ephemeridesRequest.setPage(1);

		Mockito.doReturn(1)
				.when(apiService)
				.getSpaceAssetIdByName("asset01");

		final SpaceObject spaceObject = new SpaceObject();
		Mockito.doReturn(spaceObject)
				.when(apiService)
				.getSpaceAssetEphermeridesByScn(1,
												1,
												1);

		final List<SpaceObject> results = workflowSpaceObjectCatalogService
				.getEphemeridesByAssetName(ephemeridesRequest);

		Assert.assertNotNull(results);
		Assert.assertNotNull(results.get(0));

	}

	@Test
	public void testGetTleByAssetNameEmpty() {

		EphemeridesRequest ephemeridesRequest = new EphemeridesRequest();
		ephemeridesRequest.setAssetNames(Collections.emptyList());
		ephemeridesRequest.setCount(1);
		ephemeridesRequest.setPage(1);

		Mockito.doReturn(1)
				.when(apiService)
				.getSpaceAssetIdByName("asset01");

		final SpaceObject spaceObject = new SpaceObject();
		Mockito.doReturn(spaceObject)
				.when(apiService)
				.getSpaceAssetEphermeridesByScn(1,
												1,
												1);

		final List<SpaceObject> results = workflowSpaceObjectCatalogService
				.getEphemeridesByAssetName(ephemeridesRequest);

		Assert.assertNotNull(results);
		Assert.assertTrue(results.isEmpty());

	}

}