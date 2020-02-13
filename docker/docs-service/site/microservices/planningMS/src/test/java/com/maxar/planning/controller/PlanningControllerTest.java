package com.maxar.planning.controller;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.common.exception.BadRequestException;
import com.maxar.planning.exception.PlanningQueryNoParametersException;
import com.maxar.planning.model.image.CollectionWindowModel;
import com.maxar.planning.model.tasking.TaskingModel;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:planningms.properties")
public class PlanningControllerTest
{
	private static final String EXAMPLE_MISSION_ID = "MISSION_16";

	private static final String EXAMPLE_MISSION_ID_NO_RESULTS = "NO_RESULTS";

	private static final String EXAMPLE_DATE_TASKING_START = "2020-01-01T11:00:00";

	private static final String EXAMPLE_DATE_TASKING_STOP = "2020-01-01T12:00:00";

	private static final String EXAMPLE_TARGET_ID = "P000077994";

	private static final String EXAMPLE_ASSET_NAME = "CSM3";

	private static final String EXAMPLE_ASSET_NAME_NO_RESULTS = "unknown";

	private static final Integer EXAMPLE_ASSET_SCN = 33412;

	private static final String EXAMPLE_DATE_CW_START = "2020-01-01T03:00:00";

	private static final String EXAMPLE_DATE_CW_STOP = "2020-01-01T04:00:00";

	private static final String EXAMPLE_CW_ID = "CW: 2";

	private static final String EXAMPLE_CW_ID_NO_RESULTS = "no results";

	@Autowired
	private PlanningController planningController;

	@Test
	public void testGetTasking()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<TaskingModel>> response = planningController.getTasking(	EXAMPLE_MISSION_ID,
																							EXAMPLE_DATE_TASKING_START,
																							EXAMPLE_DATE_TASKING_STOP,
																							EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_MISSION_ID,
							response.getBody()
									.get(0)
									.getMissionId());
		Assert.assertEquals(EXAMPLE_TARGET_ID,
							response.getBody()
									.get(0)
									.getLink()
									.getTargetId());
	}

	@Test
	public void testGetTaskingStartTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<TaskingModel>> response = planningController.getTasking(	EXAMPLE_MISSION_ID,
																							null,
																							EXAMPLE_DATE_TASKING_STOP,
																							EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_MISSION_ID,
							response.getBody()
									.get(0)
									.getMissionId());
		Assert.assertEquals(EXAMPLE_TARGET_ID,
							response.getBody()
									.get(0)
									.getLink()
									.getTargetId());
	}

	@Test
	public void testGetTaskingStopTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<TaskingModel>> response = planningController.getTasking(	EXAMPLE_MISSION_ID,
																							EXAMPLE_DATE_TASKING_START,
																							null,
																							EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_MISSION_ID,
							response.getBody()
									.get(0)
									.getMissionId());
		Assert.assertEquals(EXAMPLE_TARGET_ID,
							response.getBody()
									.get(0)
									.getLink()
									.getTargetId());
	}

	@Test
	public void testGetTaskingMissionIdNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<TaskingModel>> response = planningController.getTasking(	null,
																							EXAMPLE_DATE_TASKING_START,
																							EXAMPLE_DATE_TASKING_STOP,
																							EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_MISSION_ID,
							response.getBody()
									.get(0)
									.getMissionId());
		Assert.assertEquals(EXAMPLE_TARGET_ID,
							response.getBody()
									.get(0)
									.getLink()
									.getTargetId());
	}

	@Test
	public void testGetTaskingTargetIdNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<TaskingModel>> response = planningController.getTasking(	EXAMPLE_MISSION_ID,
																							EXAMPLE_DATE_TASKING_START,
																							EXAMPLE_DATE_TASKING_STOP,
																							null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(447,
							response.getBody()
									.size());
		Assert.assertTrue(response.getBody()
				.stream()
				.allMatch(tm -> tm.getMissionId()
						.equals(EXAMPLE_MISSION_ID)));
		Assert.assertTrue(response.getBody()
				.stream()
				.anyMatch(tm -> tm.getLink()
						.getTargetId()
						.equals(EXAMPLE_TARGET_ID)));
	}

	@Test
	public void testGetTaskingNoResults()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<TaskingModel>> response = planningController.getTasking(	EXAMPLE_MISSION_ID_NO_RESULTS,
																							EXAMPLE_DATE_TASKING_START,
																							EXAMPLE_DATE_TASKING_STOP,
																							EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testGetTaskingMissionIdAndTargetIdNull()
			throws PlanningQueryNoParametersException {
		planningController.getTasking(	null,
										EXAMPLE_DATE_TASKING_START,
										EXAMPLE_DATE_TASKING_STOP,
										null);
	}

	@Test(expected = BadRequestException.class)
	public void testGetTaskingStartTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getTasking(	EXAMPLE_MISSION_ID,
										"abc",
										EXAMPLE_DATE_TASKING_STOP,
										EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testGetTaskingStopTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getTasking(	EXAMPLE_MISSION_ID,
										EXAMPLE_DATE_TASKING_START,
										"asdf",
										EXAMPLE_TARGET_ID);
	}

	@Test
	public void testCountTasking()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countTasking(	EXAMPLE_MISSION_ID,
																				EXAMPLE_DATE_TASKING_START,
																				EXAMPLE_DATE_TASKING_STOP,
																				EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(1L),
							response.getBody());
	}

	@Test
	public void testCountTaskingStartTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countTasking(	EXAMPLE_MISSION_ID,
																				null,
																				EXAMPLE_DATE_TASKING_STOP,
																				EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(1L),
							response.getBody());
	}

	@Test
	public void testCountTaskingStopTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countTasking(	EXAMPLE_MISSION_ID,
																				EXAMPLE_DATE_TASKING_START,
																				null,
																				EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(1L),
							response.getBody());
	}

	@Test
	public void testCountTaskingMissionIdNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countTasking(	null,
																				EXAMPLE_DATE_TASKING_START,
																				EXAMPLE_DATE_TASKING_STOP,
																				EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(1L),
							response.getBody());
	}

	@Test
	public void testCountTaskingTargetIdNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countTasking(	EXAMPLE_MISSION_ID,
																				EXAMPLE_DATE_TASKING_START,
																				EXAMPLE_DATE_TASKING_STOP,
																				null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(447L),
							response.getBody());
	}

	@Test
	public void testCountTaskingNoResults()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countTasking(	EXAMPLE_MISSION_ID_NO_RESULTS,
																				EXAMPLE_DATE_TASKING_START,
																				EXAMPLE_DATE_TASKING_STOP,
																				EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(0L),
							response.getBody());
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testCountTaskingMissionIdAndTargetIdNull()
			throws PlanningQueryNoParametersException {
		planningController.countTasking(null,
										EXAMPLE_DATE_TASKING_START,
										EXAMPLE_DATE_TASKING_STOP,
										null);
	}

	@Test(expected = BadRequestException.class)
	public void testCountTaskingStartTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.countTasking(EXAMPLE_MISSION_ID,
										"start",
										EXAMPLE_DATE_TASKING_STOP,
										EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testCountTaskingStopTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.countTasking(EXAMPLE_MISSION_ID,
										EXAMPLE_DATE_TASKING_START,
										"stop",
										EXAMPLE_TARGET_ID);
	}

	@Test
	public void testGetTaskingPaged()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<TaskingModel>> response = planningController.getTaskingPaged(	EXAMPLE_MISSION_ID,
																								EXAMPLE_DATE_TASKING_START,
																								EXAMPLE_DATE_TASKING_STOP,
																								EXAMPLE_TARGET_ID,
																								0,
																								100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_MISSION_ID,
							response.getBody()
									.get(0)
									.getMissionId());
		Assert.assertEquals(EXAMPLE_TARGET_ID,
							response.getBody()
									.get(0)
									.getLink()
									.getTargetId());
	}

	@Test
	public void testGetTaskingPagedStartTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<TaskingModel>> response = planningController.getTaskingPaged(	EXAMPLE_MISSION_ID,
																								null,
																								EXAMPLE_DATE_TASKING_STOP,
																								EXAMPLE_TARGET_ID,
																								0,
																								100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_MISSION_ID,
							response.getBody()
									.get(0)
									.getMissionId());
		Assert.assertEquals(EXAMPLE_TARGET_ID,
							response.getBody()
									.get(0)
									.getLink()
									.getTargetId());
	}

	@Test
	public void testGetTaskingPagedStopTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<TaskingModel>> response = planningController.getTaskingPaged(	EXAMPLE_MISSION_ID,
																								EXAMPLE_DATE_TASKING_START,
																								null,
																								EXAMPLE_TARGET_ID,
																								0,
																								100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_MISSION_ID,
							response.getBody()
									.get(0)
									.getMissionId());
		Assert.assertEquals(EXAMPLE_TARGET_ID,
							response.getBody()
									.get(0)
									.getLink()
									.getTargetId());
	}

	@Test
	public void testGetTaskingPagedMissionIdNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<TaskingModel>> response = planningController.getTaskingPaged(	null,
																								EXAMPLE_DATE_TASKING_START,
																								EXAMPLE_DATE_TASKING_STOP,
																								EXAMPLE_TARGET_ID,
																								0,
																								100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_MISSION_ID,
							response.getBody()
									.get(0)
									.getMissionId());
		Assert.assertEquals(EXAMPLE_TARGET_ID,
							response.getBody()
									.get(0)
									.getLink()
									.getTargetId());
	}

	@Test
	public void testGetTaskingPagedTargetIdNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<TaskingModel>> response = planningController.getTaskingPaged(	EXAMPLE_MISSION_ID,
																								EXAMPLE_DATE_TASKING_START,
																								EXAMPLE_DATE_TASKING_STOP,
																								null,
																								0,
																								100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(100,
							response.getBody()
									.size());
		Assert.assertTrue(response.getBody()
				.stream()
				.allMatch(tm -> tm.getMissionId()
						.equals(EXAMPLE_MISSION_ID)));
	}

	@Test
	public void testGetTaskingPagedNoResults()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<TaskingModel>> response = planningController
				.getTaskingPaged(	EXAMPLE_MISSION_ID_NO_RESULTS,
									EXAMPLE_DATE_TASKING_START,
									EXAMPLE_DATE_TASKING_STOP,
									EXAMPLE_TARGET_ID,
									0,
									100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testGetTaskingPagedMissionIdAndTargetIdNull()
			throws PlanningQueryNoParametersException {
		planningController.getTaskingPaged(	null,
											EXAMPLE_DATE_TASKING_START,
											EXAMPLE_DATE_TASKING_STOP,
											null,
											0,
											100);
	}

	@Test(expected = BadRequestException.class)
	public void testGetTaskingPagedStartTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getTaskingPaged(	EXAMPLE_MISSION_ID,
											"abc",
											EXAMPLE_DATE_TASKING_STOP,
											EXAMPLE_TARGET_ID,
											0,
											100);
	}

	@Test(expected = BadRequestException.class)
	public void testGetTaskingPagedStopTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getTaskingPaged(	EXAMPLE_MISSION_ID,
											EXAMPLE_DATE_TASKING_START,
											"asdf",
											EXAMPLE_TARGET_ID,
											0,
											100);
	}

	@Test(expected = BadRequestException.class)
	public void testGetTaskingPagedPageInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getTaskingPaged(	EXAMPLE_MISSION_ID,
											EXAMPLE_DATE_TASKING_START,
											EXAMPLE_DATE_TASKING_STOP,
											EXAMPLE_TARGET_ID,
											-1,
											100);
	}

	@Test(expected = BadRequestException.class)
	public void testGetTaskingPagedCountInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getTaskingPaged(	EXAMPLE_MISSION_ID,
											EXAMPLE_DATE_TASKING_START,
											EXAMPLE_DATE_TASKING_STOP,
											EXAMPLE_TARGET_ID,
											0,
											0);
	}

	@Test
	public void testGetCollectionWindows()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindows(	EXAMPLE_ASSET_NAME,
										EXAMPLE_ASSET_SCN,
										EXAMPLE_DATE_CW_START,
										EXAMPLE_DATE_CW_STOP,
										EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.get(0)
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowsNameNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController.getCollectionWindows(	null,
																												EXAMPLE_ASSET_SCN,
																												EXAMPLE_DATE_CW_START,
																												EXAMPLE_DATE_CW_STOP,
																												EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.get(0)
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowsNameOnly()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindows(	EXAMPLE_ASSET_NAME,
										null,
										null,
										null,
										null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(35,
							response.getBody()
									.size());
	}

	@Test
	public void testGetCollectionWindowsScnNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindows(	EXAMPLE_ASSET_NAME,
										null,
										EXAMPLE_DATE_CW_START,
										EXAMPLE_DATE_CW_STOP,
										EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.get(0)
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowsScnOnly()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController.getCollectionWindows(	null,
																												EXAMPLE_ASSET_SCN,
																												null,
																												null,
																												null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(35,
							response.getBody()
									.size());
	}

	@Test
	public void testGetCollectionWindowsStartTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindows(	EXAMPLE_ASSET_NAME,
										EXAMPLE_ASSET_SCN,
										null,
										EXAMPLE_DATE_CW_STOP,
										EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.get(0)
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowsStopTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindows(	EXAMPLE_ASSET_NAME,
										EXAMPLE_ASSET_SCN,
										EXAMPLE_DATE_CW_START,
										null,
										EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.get(0)
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowsStartTimeAndStopTimeOnly()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController.getCollectionWindows(	null,
																												null,
																												EXAMPLE_DATE_CW_START,
																												EXAMPLE_DATE_CW_STOP,
																												null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(94,
							response.getBody()
									.size());
	}

	@Test
	public void testGetCollectionWindowsTargetIdNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindows(	EXAMPLE_ASSET_NAME,
										EXAMPLE_ASSET_SCN,
										EXAMPLE_DATE_CW_START,
										EXAMPLE_DATE_CW_STOP,
										null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(35,
							response.getBody()
									.size());
		Assert.assertTrue(response.getBody()
				.stream()
				.map(CollectionWindowModel::getCwId)
				.allMatch(EXAMPLE_CW_ID::equals));
	}

	@Test
	public void testGetCollectionWindowsNoResults()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindows(	EXAMPLE_ASSET_NAME_NO_RESULTS,
										EXAMPLE_ASSET_SCN,
										EXAMPLE_DATE_CW_START,
										EXAMPLE_DATE_CW_STOP,
										EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testGetCollectionWindowsNoParameters()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindows(null,
												null,
												null,
												null,
												EXAMPLE_TARGET_ID);
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testGetCollectionWindowsStartTimeOnly()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindows(null,
												null,
												EXAMPLE_DATE_CW_START,
												null,
												EXAMPLE_TARGET_ID);
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testGetCollectionWindowsStopTimeOnly()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindows(null,
												null,
												null,
												EXAMPLE_DATE_CW_STOP,
												EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowsStartTimeEmpty()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindows(EXAMPLE_ASSET_NAME,
												EXAMPLE_ASSET_SCN,
												"",
												EXAMPLE_DATE_CW_STOP,
												EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowsStartTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindows(EXAMPLE_ASSET_NAME,
												EXAMPLE_ASSET_SCN,
												"start",
												EXAMPLE_DATE_CW_STOP,
												EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowsStopTimeEmpty()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindows(EXAMPLE_ASSET_NAME,
												EXAMPLE_ASSET_SCN,
												EXAMPLE_DATE_CW_START,
												"",
												EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowsStopTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindows(EXAMPLE_ASSET_NAME,
												EXAMPLE_ASSET_SCN,
												EXAMPLE_DATE_CW_START,
												"stop",
												EXAMPLE_TARGET_ID);
	}

	@Test
	public void testGetCollectionWindow()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<CollectionWindowModel> response = planningController.getCollectionWindow(	EXAMPLE_CW_ID,
																										EXAMPLE_ASSET_NAME,
																										EXAMPLE_ASSET_SCN,
																										EXAMPLE_DATE_CW_START,
																										EXAMPLE_DATE_CW_STOP,
																										EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowNameNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<CollectionWindowModel> response = planningController.getCollectionWindow(	EXAMPLE_CW_ID,
																										null,
																										EXAMPLE_ASSET_SCN,
																										EXAMPLE_DATE_CW_START,
																										EXAMPLE_DATE_CW_STOP,
																										EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowNameOnly()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<CollectionWindowModel> response = planningController.getCollectionWindow(	EXAMPLE_CW_ID,
																										EXAMPLE_ASSET_NAME,
																										null,
																										null,
																										null,
																										null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowScnNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<CollectionWindowModel> response = planningController.getCollectionWindow(	EXAMPLE_CW_ID,
																										EXAMPLE_ASSET_NAME,
																										null,
																										EXAMPLE_DATE_CW_START,
																										EXAMPLE_DATE_CW_STOP,
																										EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowScnOnly()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<CollectionWindowModel> response = planningController.getCollectionWindow(	EXAMPLE_CW_ID,
																										null,
																										EXAMPLE_ASSET_SCN,
																										null,
																										null,
																										null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowStartTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<CollectionWindowModel> response = planningController.getCollectionWindow(	EXAMPLE_CW_ID,
																										EXAMPLE_ASSET_NAME,
																										EXAMPLE_ASSET_SCN,
																										null,
																										EXAMPLE_DATE_CW_STOP,
																										EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowStopTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<CollectionWindowModel> response = planningController.getCollectionWindow(	EXAMPLE_CW_ID,
																										EXAMPLE_ASSET_NAME,
																										EXAMPLE_ASSET_SCN,
																										EXAMPLE_DATE_CW_START,
																										null,
																										EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowStartTimeAndStopTimeOnly()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<CollectionWindowModel> response = planningController.getCollectionWindow(	EXAMPLE_CW_ID,
																										null,
																										null,
																										EXAMPLE_DATE_CW_START,
																										EXAMPLE_DATE_CW_STOP,
																										null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowTargetIdNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<CollectionWindowModel> response = planningController.getCollectionWindow(	EXAMPLE_CW_ID,
																										EXAMPLE_ASSET_NAME,
																										EXAMPLE_ASSET_SCN,
																										EXAMPLE_DATE_CW_START,
																										EXAMPLE_DATE_CW_STOP,
																										null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowAssetNameNoResults()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<CollectionWindowModel> response = planningController.getCollectionWindow(	EXAMPLE_CW_ID,
																										EXAMPLE_ASSET_NAME_NO_RESULTS,
																										EXAMPLE_ASSET_SCN,
																										EXAMPLE_DATE_CW_START,
																										EXAMPLE_DATE_CW_STOP,
																										EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test
	public void testGetCollectionWindowCwIdNoResults()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<CollectionWindowModel> response = planningController
				.getCollectionWindow(	EXAMPLE_CW_ID_NO_RESULTS,
										EXAMPLE_ASSET_NAME,
										EXAMPLE_ASSET_SCN,
										EXAMPLE_DATE_CW_START,
										EXAMPLE_DATE_CW_STOP,
										EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testGetCollectionWindowNoParameters()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindow(	EXAMPLE_CW_ID,
												null,
												null,
												null,
												null,
												EXAMPLE_TARGET_ID);
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testGetCollectionWindowStartTimeOnly()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindow(	EXAMPLE_CW_ID,
												null,
												null,
												EXAMPLE_DATE_CW_START,
												null,
												EXAMPLE_TARGET_ID);
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testGetCollectionWindowStopTimeOnly()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindow(	EXAMPLE_CW_ID,
												null,
												null,
												null,
												EXAMPLE_DATE_CW_STOP,
												EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowStartTimeEmpty()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindow(	EXAMPLE_CW_ID,
												EXAMPLE_ASSET_NAME,
												EXAMPLE_ASSET_SCN,
												"",
												EXAMPLE_DATE_CW_STOP,
												EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowStartTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindow(	EXAMPLE_CW_ID,
												EXAMPLE_ASSET_NAME,
												EXAMPLE_ASSET_SCN,
												"START TIME",
												EXAMPLE_DATE_CW_STOP,
												EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowStopTimeEmpty()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindow(	EXAMPLE_CW_ID,
												EXAMPLE_ASSET_NAME,
												EXAMPLE_ASSET_SCN,
												EXAMPLE_DATE_CW_START,
												"",
												EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowStopTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindow(	EXAMPLE_CW_ID,
												EXAMPLE_ASSET_NAME,
												EXAMPLE_ASSET_SCN,
												EXAMPLE_DATE_CW_START,
												"STOP TIME",
												EXAMPLE_TARGET_ID);
	}

	@Test
	public void testGetCollectionWindowsPaged()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME,
											EXAMPLE_ASSET_SCN,
											EXAMPLE_DATE_CW_START,
											EXAMPLE_DATE_CW_STOP,
											EXAMPLE_TARGET_ID,
											0,
											100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.get(0)
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowsPagedNameNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController.getCollectionWindowsPaged(	null,
																													EXAMPLE_ASSET_SCN,
																													EXAMPLE_DATE_CW_START,
																													EXAMPLE_DATE_CW_STOP,
																													EXAMPLE_TARGET_ID,
																													0,
																													100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.get(0)
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowsPagedNameOnly()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME,
											null,
											null,
											null,
											null,
											0,
											100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(35,
							response.getBody()
									.size());
	}

	@Test
	public void testGetCollectionWindowsPagedScnNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME,
											null,
											EXAMPLE_DATE_CW_START,
											EXAMPLE_DATE_CW_STOP,
											EXAMPLE_TARGET_ID,
											0,
											100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.get(0)
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowsPagedScnOnly()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController.getCollectionWindowsPaged(	null,
																													EXAMPLE_ASSET_SCN,
																													null,
																													null,
																													null,
																													0,
																													100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(35,
							response.getBody()
									.size());
	}

	@Test
	public void testGetCollectionWindowsPagedStartTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME,
											EXAMPLE_ASSET_SCN,
											null,
											EXAMPLE_DATE_CW_STOP,
											EXAMPLE_TARGET_ID,
											0,
											100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.get(0)
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowsPagedStopTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME,
											EXAMPLE_ASSET_SCN,
											EXAMPLE_DATE_CW_START,
											null,
											EXAMPLE_TARGET_ID,
											0,
											100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_CW_ID,
							response.getBody()
									.get(0)
									.getCwId());
	}

	@Test
	public void testGetCollectionWindowsPagedStartTimeAndStopTimeOnly()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController.getCollectionWindowsPaged(	null,
																													null,
																													EXAMPLE_DATE_CW_START,
																													EXAMPLE_DATE_CW_STOP,
																													null,
																													0,
																													50);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(50,
							response.getBody()
									.size());
	}

	@Test
	public void testGetCollectionWindowsPagedTargetIdNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME,
											EXAMPLE_ASSET_SCN,
											EXAMPLE_DATE_CW_START,
											EXAMPLE_DATE_CW_STOP,
											null,
											0,
											100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(35,
							response.getBody()
									.size());
		Assert.assertTrue(response.getBody()
				.stream()
				.map(CollectionWindowModel::getCwId)
				.allMatch(EXAMPLE_CW_ID::equals));
	}

	@Test
	public void testGetCollectionWindowsPagedNoResults()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<List<CollectionWindowModel>> response = planningController
				.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME_NO_RESULTS,
											EXAMPLE_ASSET_SCN,
											EXAMPLE_DATE_CW_START,
											EXAMPLE_DATE_CW_STOP,
											EXAMPLE_TARGET_ID,
											0,
											100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testGetCollectionWindowsPagedNoParameters()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindowsPaged(	null,
														null,
														null,
														null,
														EXAMPLE_TARGET_ID,
														0,
														100);
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testGetCollectionWindowsPagedStartTimeOnly()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindowsPaged(	null,
														null,
														EXAMPLE_DATE_CW_START,
														null,
														EXAMPLE_TARGET_ID,
														0,
														100);
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testGetCollectionWindowsPagedStopTimeOnly()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindowsPaged(	null,
														null,
														null,
														EXAMPLE_DATE_CW_STOP,
														EXAMPLE_TARGET_ID,
														0,
														100);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowsPagedStartTimeEmpty()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME,
														EXAMPLE_ASSET_SCN,
														"",
														EXAMPLE_DATE_CW_STOP,
														EXAMPLE_TARGET_ID,
														0,
														100);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowsPagedStartTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME,
														EXAMPLE_ASSET_SCN,
														"start",
														EXAMPLE_DATE_CW_STOP,
														EXAMPLE_TARGET_ID,
														0,
														100);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowsPagedStopTimeEmpty()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME,
														EXAMPLE_ASSET_SCN,
														EXAMPLE_DATE_CW_START,
														"",
														EXAMPLE_TARGET_ID,
														0,
														100);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowsPagedStopTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME,
														EXAMPLE_ASSET_SCN,
														EXAMPLE_DATE_CW_START,
														"stop",
														EXAMPLE_TARGET_ID,
														0,
														100);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowsPagedPageInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME,
														EXAMPLE_ASSET_SCN,
														EXAMPLE_DATE_CW_START,
														EXAMPLE_DATE_CW_STOP,
														EXAMPLE_TARGET_ID,
														-1,
														100);
	}

	@Test(expected = BadRequestException.class)
	public void testGetCollectionWindowsPagedCountInvalid()
			throws PlanningQueryNoParametersException {
		planningController.getCollectionWindowsPaged(	EXAMPLE_ASSET_NAME,
														EXAMPLE_ASSET_SCN,
														EXAMPLE_DATE_CW_START,
														EXAMPLE_DATE_CW_STOP,
														EXAMPLE_TARGET_ID,
														0,
														0);
	}

	@Test
	public void testCountCollectionWindows()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countCollectionWindows(EXAMPLE_ASSET_NAME,
																						EXAMPLE_ASSET_SCN,
																						EXAMPLE_DATE_CW_START,
																						EXAMPLE_DATE_CW_STOP,
																						EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(1L),
							response.getBody());
	}

	@Test
	public void testCountCollectionWindowsNameNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countCollectionWindows(null,
																						EXAMPLE_ASSET_SCN,
																						EXAMPLE_DATE_CW_START,
																						EXAMPLE_DATE_CW_STOP,
																						EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(1L),
							response.getBody());
	}

	@Test
	public void testCountCollectionWindowsNameOnly()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countCollectionWindows(EXAMPLE_ASSET_NAME,
																						null,
																						null,
																						null,
																						null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(35L),
							response.getBody());
	}

	@Test
	public void testCountCollectionWindowsScnNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countCollectionWindows(EXAMPLE_ASSET_NAME,
																						null,
																						EXAMPLE_DATE_CW_START,
																						EXAMPLE_DATE_CW_STOP,
																						EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(1L),
							response.getBody());
	}

	@Test
	public void testCountCollectionWindowsScnOnly()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countCollectionWindows(null,
																						EXAMPLE_ASSET_SCN,
																						null,
																						null,
																						null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(35L),
							response.getBody());
	}

	@Test
	public void testCountCollectionWindowsStartTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countCollectionWindows(EXAMPLE_ASSET_NAME,
																						EXAMPLE_ASSET_SCN,
																						null,
																						EXAMPLE_DATE_CW_STOP,
																						EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(1L),
							response.getBody());
	}

	@Test
	public void testCountCollectionWindowsStopTimeNull()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countCollectionWindows(EXAMPLE_ASSET_NAME,
																						EXAMPLE_ASSET_SCN,
																						EXAMPLE_DATE_CW_START,
																						null,
																						EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(1L),
							response.getBody());
	}

	@Test
	public void testCountCollectionWindowsStartTimeAndStopTimeOnly()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countCollectionWindows(null,
																						null,
																						EXAMPLE_DATE_CW_START,
																						EXAMPLE_DATE_CW_STOP,
																						null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(94L),
							response.getBody());
	}

	@Test
	public void testCountCollectionWindowsNoResults()
			throws PlanningQueryNoParametersException {
		final ResponseEntity<Long> response = planningController.countCollectionWindows(EXAMPLE_ASSET_NAME_NO_RESULTS,
																						EXAMPLE_ASSET_SCN,
																						EXAMPLE_DATE_CW_START,
																						EXAMPLE_DATE_CW_STOP,
																						EXAMPLE_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(0L),
							response.getBody());
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testCountCollectionWindowsNoParameters()
			throws PlanningQueryNoParametersException {
		planningController.countCollectionWindows(	null,
													null,
													null,
													null,
													EXAMPLE_TARGET_ID);
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testCountCollectionWindowsStartTimeOnly()
			throws PlanningQueryNoParametersException {
		planningController.countCollectionWindows(	null,
													null,
													EXAMPLE_DATE_CW_START,
													null,
													EXAMPLE_TARGET_ID);
	}

	@Test(expected = PlanningQueryNoParametersException.class)
	public void testCountCollectionWindowsStopTimeOnly()
			throws PlanningQueryNoParametersException {
		planningController.countCollectionWindows(	null,
													null,
													null,
													EXAMPLE_DATE_CW_STOP,
													EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testCountCollectionWindowsStartTimeEmpty()
			throws PlanningQueryNoParametersException {
		planningController.countCollectionWindows(	EXAMPLE_ASSET_NAME,
													EXAMPLE_ASSET_SCN,
													"",
													EXAMPLE_DATE_CW_STOP,
													EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testCountCollectionWindowsStartTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.countCollectionWindows(	EXAMPLE_ASSET_NAME,
													EXAMPLE_ASSET_SCN,
													"not allowed",
													EXAMPLE_DATE_CW_STOP,
													EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testCountCollectionWindowsStopTimeEmpty()
			throws PlanningQueryNoParametersException {
		planningController.countCollectionWindows(	EXAMPLE_ASSET_NAME,
													EXAMPLE_ASSET_SCN,
													EXAMPLE_DATE_CW_START,
													"",
													EXAMPLE_TARGET_ID);
	}

	@Test(expected = BadRequestException.class)
	public void testCountCollectionWindowsStopTimeInvalid()
			throws PlanningQueryNoParametersException {
		planningController.countCollectionWindows(	EXAMPLE_ASSET_NAME,
													EXAMPLE_ASSET_SCN,
													EXAMPLE_DATE_CW_START,
													"invalid",
													EXAMPLE_TARGET_ID);
	}
}
