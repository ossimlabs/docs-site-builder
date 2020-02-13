package com.maxar.workflow;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.workflow.controller.WorkflowAccessController;
import com.maxar.workflow.controller.WorkflowAssetController;
import com.maxar.workflow.controller.WorkflowMapController;
import com.maxar.workflow.controller.WorkflowTargetController;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:testworkflowms.properties")
public class WorkflowServiceApplicationTest
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void contextLoads() {
		final WorkflowAssetController workflowAssetController = applicationContext
				.getBean(WorkflowAssetController.class);

		Assert.assertNotNull(workflowAssetController);


		final WorkflowMapController workflowMapController = applicationContext.getBean(WorkflowMapController.class);

		Assert.assertNotNull(workflowMapController);

		final WorkflowTargetController workflowTargetController = applicationContext.getBean(WorkflowTargetController.class);

		Assert.assertNotNull(workflowTargetController);

		final WorkflowAccessController workflowAccessController = applicationContext.getBean(WorkflowAccessController.class);

		Assert.assertNotNull(workflowAccessController);

	}
}
