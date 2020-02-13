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

import com.maxar.common.exception.BadRequestException;
import com.maxar.target.model.TargetModel;
import com.maxar.target.model.TargetType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testworkflowms.properties")
public class WorkflowTargetServiceTest
{
	private static final String EXAMPLE_WKT = "POINT(0.0 0.0)";

	private static final String EXAMPLE_TARGET_ID = "target000";

	@Autowired
	private WorkflowTargetService workflowTargetService;

	@MockBean
	private ApiService apiService;

	@Test
	public void testGetTargetsByGeometryEmpty() {
		Mockito.when(apiService.getTargetsByGeometry(Mockito.eq(EXAMPLE_WKT)))
				.thenReturn(Collections.emptyList());

		final List<TargetModel> targets = workflowTargetService.getTargetsByGeometry(EXAMPLE_WKT);

		Assert.assertNotNull(targets);
		Assert.assertTrue(targets.isEmpty());
	}

	@Test
	public void testGetTargetsByGeometryOne() {
		final TargetModel targetModel = TargetModel.builder()
				.targetType(TargetType.POINT)
				.targetId(EXAMPLE_TARGET_ID)
				.build();

		Mockito.when(apiService.getTargetsByGeometry(Mockito.eq(EXAMPLE_WKT)))
				.thenReturn(Collections.singletonList(targetModel));

		final List<TargetModel> targets = workflowTargetService.getTargetsByGeometry(EXAMPLE_WKT);

		Assert.assertNotNull(targets);
		Assert.assertEquals(1,
							targets.size());
		Assert.assertEquals(TargetType.POINT,
							targetModel.getTargetType());
		Assert.assertEquals(EXAMPLE_TARGET_ID,
							targetModel.getTargetId());
	}

	@Test(expected = BadRequestException.class)
	public void testGetTargetsByGeometryGeometryEmpty() {
		workflowTargetService.getTargetsByGeometry("");
	}
}
