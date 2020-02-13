package com.maxar.workflow.controller;

import java.util.Collections;
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

import com.maxar.common.exception.BadRequestException;
import com.maxar.target.model.TargetModel;
import com.maxar.workflow.service.WorkflowTargetService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testworkflowms.properties")
public class WorkflowTargetControllerTest
{
	@Autowired
	private WorkflowTargetController workflowTargetController;

	@MockBean
	private WorkflowTargetService workflowTargetService;

	@Test
	public void testTargetsByGeometry() {

		final String geometryWkt = "POINT(0 0 0)";
		Mockito.when(workflowTargetService.getTargetsByGeometry(Mockito.eq(geometryWkt)))
				.thenReturn(Collections.singletonList(new TargetModel()));

		final ResponseEntity<List<TargetModel>> response = workflowTargetController.getTargetsByGeometry(geometryWkt);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertNotNull(response.getBody()
				.get(0));

	}

	@Test(expected = BadRequestException.class)
	public void testTargetsByGeometryInvalidRequestException() {
		final String geometryWkt = "PINT(0 0 0)";

		Mockito.when(workflowTargetService.getTargetsByGeometry(Mockito.eq(geometryWkt)))
				.thenThrow(new BadRequestException(
						"Unable to parse WKT: " + geometryWkt));

		workflowTargetService.getTargetsByGeometry(geometryWkt);
	}

}
