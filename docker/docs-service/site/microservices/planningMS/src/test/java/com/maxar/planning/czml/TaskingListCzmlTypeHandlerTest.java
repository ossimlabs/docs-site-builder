package com.maxar.planning.czml;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.planning.model.link.LinkModel;
import com.maxar.planning.model.tasking.TaskingModel;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:planningms.properties")
public class TaskingListCzmlTypeHandlerTest
{
	@Autowired
	private TaskingListCzmlTypeHandler taskingListCzmlTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(taskingListCzmlTypeHandler.canHandle(TaskingModel.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(taskingListCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertTrue(taskingListCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandle() {
		final TaskingModel taskingModel = new TaskingModel();
		taskingModel.setMissionId("mission0");
		taskingModel.setComplex(false);
		taskingModel.setEarliestImageTimeMillis(0L);
		taskingModel.setLatestImageTimeMillis(1000L);
		taskingModel.setNumImages(1);
		taskingModel.setPriority(0);
		taskingModel.setTotTimeMillis(100L);
		taskingModel.setSensorName("sensor0");

		final LinkModel linkModel = new LinkModel();
		linkModel.setTargetId("target0");
		linkModel.setCrId("cr0");

		taskingModel.setLink(linkModel);

		final List<String> czmlList = taskingListCzmlTypeHandler.handle(Collections.singletonList(taskingModel));

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(1,
							czmlList.size());
		Assert.assertTrue(czmlList.get(0)
				.contains("\"id\":\"mission0\""));
		Assert.assertTrue(czmlList.get(0)
				.contains("\"name\":\"mission0\""));
	}
}
