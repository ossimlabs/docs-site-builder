package com.maxar.mission.czml;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.mission.model.MissionModel;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testmissionms.properties")
public class MissionCzmlTypeHandlerTest
{
	@Autowired
	private MissionCzmlTypeHandler missionCzmlTypeHandler;

	@Test
	public void testCanHandle() {
		Assert.assertTrue(missionCzmlTypeHandler.canHandle(MissionModel.class));
	}

	@Test
	public void testCanHandleFalse() {
		Assert.assertFalse(missionCzmlTypeHandler.canHandle(String.class));
	}

	@Test
	public void testHandlesIterable() {
		Assert.assertFalse(missionCzmlTypeHandler.handlesIterable());
	}

	@Test
	public void testHandle() {
		final MissionModel mission = new MissionModel();

		final List<String> czmlList = missionCzmlTypeHandler.handle(mission);

		Assert.assertNotNull(czmlList);
		Assert.assertEquals(1,
							czmlList.size());
		Assert.assertTrue(czmlList.get(0)
				.contains("\"name\":\"Mission null Asset null\""));
	}
}
