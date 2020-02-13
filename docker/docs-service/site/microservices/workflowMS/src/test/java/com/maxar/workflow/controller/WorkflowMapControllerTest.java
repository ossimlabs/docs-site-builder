package com.maxar.workflow.controller;

import java.util.Collections;

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

import com.maxar.workflow.model.CzmlErrorReporterWithId;
import com.maxar.workflow.service.WorkflowMapService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testworkflowms.properties")
public class WorkflowMapControllerTest
{
	private static final String EXAMPLE_SESSION = "session";

	private static final String EXAMPLE_CZML_ERROR_ID = "error";

	private static final String EXAMPLE_CZML_ERROR_STRING = "error generating CZML";

	@Autowired
	private WorkflowMapController workflowMapController;

	@MockBean
	private WorkflowMapService workflowMapService;

	@Test
	public void testDeleteCzml() {
		Mockito.when(workflowMapService.deleteAllCzml(Mockito.eq(EXAMPLE_SESSION)))
				.thenReturn(null);

		final ResponseEntity<CzmlErrorReporterWithId> response = workflowMapController.deleteCzml(EXAMPLE_SESSION);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test
	public void testDeleteCzmlFailure() {
		final CzmlErrorReporterWithId czmlErrorReporterWithId = new CzmlErrorReporterWithId();
		czmlErrorReporterWithId.setId(EXAMPLE_CZML_ERROR_ID);
		czmlErrorReporterWithId.setCzmlError(EXAMPLE_CZML_ERROR_STRING);

		Mockito.when(workflowMapService.deleteAllCzml(Mockito.eq(EXAMPLE_SESSION)))
				.thenReturn(czmlErrorReporterWithId);

		final ResponseEntity<CzmlErrorReporterWithId> response = workflowMapController.deleteCzml(EXAMPLE_SESSION);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_CZML_ERROR_ID,
							response.getBody()
									.getId());
		Assert.assertEquals(EXAMPLE_CZML_ERROR_STRING,
							response.getBody()
									.getCzmlError());
	}

	@Test
	public void testDisplayCzml() {
		Mockito.when(workflowMapService.displayCzml(Mockito.eq(EXAMPLE_SESSION),
													Mockito.anyList()))
				.thenReturn(null);

		final ResponseEntity<CzmlErrorReporterWithId> response = workflowMapController
				.displayCzml(	Collections.emptyList(),
								EXAMPLE_SESSION);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test
	public void testDisplayCzmlFailure() {
		final CzmlErrorReporterWithId czmlErrorReporterWithId = new CzmlErrorReporterWithId();
		czmlErrorReporterWithId.setId(EXAMPLE_CZML_ERROR_ID);
		czmlErrorReporterWithId.setCzmlError(EXAMPLE_CZML_ERROR_STRING);

		Mockito.when(workflowMapService.displayCzml(Mockito.eq(EXAMPLE_SESSION),
													Mockito.anyList()))
				.thenReturn(czmlErrorReporterWithId);

		final ResponseEntity<CzmlErrorReporterWithId> response = workflowMapController
				.displayCzml(	Collections.emptyList(),
								EXAMPLE_SESSION);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_CZML_ERROR_ID,
							response.getBody()
									.getId());
		Assert.assertEquals(EXAMPLE_CZML_ERROR_STRING,
							response.getBody()
									.getCzmlError());
	}
}