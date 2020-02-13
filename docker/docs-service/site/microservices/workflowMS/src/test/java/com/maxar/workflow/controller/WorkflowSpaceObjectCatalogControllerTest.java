package com.maxar.workflow.controller;

import java.util.ArrayList;
import java.util.List;

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

import com.maxar.ephemeris.model.EphemerisModel;
import com.maxar.ephemeris.model.TLEModel;
import com.maxar.spaceobjectcatalog.model.SpaceObject;
import com.maxar.workflow.model.EphemeridesRequest;
import com.maxar.workflow.service.WorkflowSpaceObjectCatalogService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testworkflowms.properties")
public class WorkflowSpaceObjectCatalogControllerTest
{

	@Autowired
	private WorkflowSpaceObjectCatalogController workflowSpaceObjectCatalogController;

	@MockBean
	private WorkflowSpaceObjectCatalogService workflowSpaceObjectCatalogService;

	@Test
	public void testGetEphemeridesForAssets() {

		final List<String> names = new ArrayList<String>();
		names.add("CSM1");
		EphemeridesRequest ephemeridesRequest = new EphemeridesRequest();
		ephemeridesRequest.setAssetNames(names);
		ephemeridesRequest.setCount(1);
		ephemeridesRequest.setPage(1);

		final List<Integer> responseScns = new ArrayList<Integer>();
		responseScns.add(31598);
		Mockito.when(workflowSpaceObjectCatalogService.getScnForAssetsByName(ephemeridesRequest))
				.thenReturn(responseScns);

		final List<SpaceObject> responseSpaceObject = new ArrayList<SpaceObject>();
		final SpaceObject spaceObject = new SpaceObject();
		spaceObject.setScn(31598);
		final List<EphemerisModel> ephemerides = new ArrayList<EphemerisModel>();

		final TLEModel tle1 = new TLEModel();
		tle1.setScn(31598);
		tle1.setDescription("COSMO-SKYMED 1");
		tle1.setTleLineOne("1 31598U 07023A   19365.34283667  .00000226  00000-0  34961-4 0  9994");
		tle1.setTleLineTwo("2 31598  97.8920  84.4159 0001527  78.3741 281.7653 14.82149700664520");
		ephemerides.add(tle1);

		final TLEModel tle2 = new TLEModel();
		tle2.setScn(31598);
		tle2.setDescription("COSMO-SKYMED 1");
		tle2.setTleLineOne("3 31598U 07023A   19365.34285767  .00300227  10000-4  34961-5 0  9995");
		tle2.setTleLineTwo("4 31598  97.8920  85.4159 0001527  78.3741 281.7653 14.82149700664526");
		ephemerides.add(tle2);

		spaceObject.setEphemerides(ephemerides);
		responseSpaceObject.add(spaceObject);

		Mockito.when(workflowSpaceObjectCatalogService.getEphemeridesByAssetName(ephemeridesRequest))
				.thenReturn(responseSpaceObject);

		final ResponseEntity<List<SpaceObject>> response = workflowSpaceObjectCatalogController
				.getEphemeridesForAssets(ephemeridesRequest);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals((Integer) 31598,
							response.getBody()
									.get(0)
									.getScn());
		Assert.assertEquals(ephemerides,
							response.getBody()
									.get(0)
									.getEphemerides());
		Assert.assertEquals(tle1,
							response.getBody()
									.get(0)
									.getEphemerides()
									.get(0));
		Assert.assertEquals(tle2,
							response.getBody()
									.get(0)
									.getEphemerides()
									.get(1));

	}

	@Test
	public void testGetEphemeridesForAssetsEmpty() {

		EphemeridesRequest ephemeridesRequest = new EphemeridesRequest();
		ephemeridesRequest.setCount(1);
		ephemeridesRequest.setPage(1);

		final List<Integer> responseScns = new ArrayList<Integer>();
		Mockito.when(workflowSpaceObjectCatalogService.getScnForAssetsByName(ephemeridesRequest))
				.thenReturn(responseScns);

		final List<SpaceObject> responseSpaceObject = new ArrayList<SpaceObject>();
		Mockito.when(workflowSpaceObjectCatalogService.getEphemeridesByAssetName(ephemeridesRequest))
				.thenReturn(responseSpaceObject);

		final ResponseEntity<List<SpaceObject>> response = workflowSpaceObjectCatalogController
				.getEphemeridesForAssets(ephemeridesRequest);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}
}
