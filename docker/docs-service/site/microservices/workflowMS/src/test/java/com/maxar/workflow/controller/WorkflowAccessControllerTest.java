package com.maxar.workflow.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.access.model.AccessValues;
import com.maxar.workflow.model.Access;
import com.maxar.workflow.model.AccessDetailsRequest;
import com.maxar.workflow.model.AccessTarget;
import com.maxar.workflow.model.TargetAccessRequest;
import com.maxar.workflow.model.TargetGeometryAccessRequest;
import com.maxar.workflow.service.WorkflowAccessService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testworkflowms.properties")
public class WorkflowAccessControllerTest
{
	private static final String EXAMPLE_CONSTRAINT_NAME = "constraint";

	@Autowired
	private WorkflowAccessController workflowAccessController;

	@MockBean
	private WorkflowAccessService workflowAccessService;

	@Test
	public void testGetAccessesForTarget() {
		final Access access = new Access();
		final TargetAccessRequest accessRequest = new TargetAccessRequest();
		accessRequest.setTargetIds(Collections.singletonList("target0"));
		accessRequest.setSpaceAssetIds(Collections.singletonList("space0"));
		accessRequest.setStart("2019-06-01T16:00:00");
		accessRequest.setStop("2019-06-03T19:25:00");

		Mockito.when(workflowAccessService.getAccessesForTargetsAndAssets(Mockito.eq(accessRequest)))
				.thenReturn(Collections.singletonList(access));

		final ResponseEntity<List<Access>> response = workflowAccessController.getAccessesForTarget(accessRequest);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(access,
							response.getBody()
									.get(0));
	}

	@Test
	public void testGetNoAccessesForTarget() {
		final TargetAccessRequest accessRequest = new TargetAccessRequest();
		accessRequest.setTargetIds(Collections.singletonList("target0"));
		accessRequest.setSpaceAssetIds(Collections.singletonList("space0"));
		accessRequest.setStart("2019-06-01T16:00:00");
		accessRequest.setStop("2019-06-03T19:25:00");

		Mockito.when(workflowAccessService.getAccessesForTargetsAndAssets(Mockito.eq(accessRequest)))
				.thenReturn(new ArrayList<>());

		final ResponseEntity<List<Access>> response = workflowAccessController.getAccessesForTarget(accessRequest);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(0,
							response.getBody()
									.size());
	}

	@Test
	public void testGetAccessesForTargetGeometry() {
		final Access access = new Access();

		final AccessTarget accessTarget = new AccessTarget();
		accessTarget.setTargetId("target0");
		accessTarget.setGeometry("POINT(0.0 0.0)");

		final TargetGeometryAccessRequest accessRequest = new TargetGeometryAccessRequest();
		accessRequest.setTargets(Collections.singletonList(accessTarget));
		accessRequest.setSpaceAssetIds(Collections.singletonList("space0"));
		accessRequest.setStart("2019-06-01T16:00:00");
		accessRequest.setStop("2019-06-03T19:25:00");

		Mockito.when(workflowAccessService.getAccessesForTargetGeometriesAndAssets(Mockito.eq(accessRequest)))
				.thenReturn(Collections.singletonList(access));

		final ResponseEntity<List<Access>> response = workflowAccessController
				.getAccessesForTargetGeometry(accessRequest);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(access,
							response.getBody()
									.get(0));
	}

	@Test
	public void testGetNoAccessesForTargetGeometry() {
		final AccessTarget accessTarget = new AccessTarget();
		accessTarget.setTargetId("target0");
		accessTarget.setGeometry("POINT(0.0 0.0)");

		final TargetGeometryAccessRequest accessRequest = new TargetGeometryAccessRequest();
		accessRequest.setTargets(Collections.singletonList(accessTarget));
		accessRequest.setSpaceAssetIds(Collections.singletonList("space0"));
		accessRequest.setStart("2019-06-01T16:00:00");
		accessRequest.setStop("2019-06-03T19:25:00");

		Mockito.when(workflowAccessService.getAccessesForTargetGeometriesAndAssets(Mockito.eq(accessRequest)))
				.thenReturn(new ArrayList<>());

		final ResponseEntity<List<Access>> response = workflowAccessController
				.getAccessesForTargetGeometry(accessRequest);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(0,
							response.getBody()
									.size());
	}

	@Test
	public void testGetAccessConstraintNames() {
		Mockito.when(workflowAccessService.getAccessConstraintNames())
				.thenReturn(Collections.singletonList(EXAMPLE_CONSTRAINT_NAME));

		final ResponseEntity<List<String>> response = workflowAccessController.getAccessConstraintNames();

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_CONSTRAINT_NAME,
							response.getBody()
									.get(0));
	}

	@Test
	public void testGetAccessDetails() {
		final AccessDetailsRequest accessDetailsRequest = new AccessDetailsRequest();
		Mockito.when(workflowAccessService.getAccessDetails(Mockito.eq(accessDetailsRequest)))
				.thenReturn(new AccessValues());

		final ResponseEntity<AccessValues> response = workflowAccessController.getAccessDetails(accessDetailsRequest);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
	}
}
