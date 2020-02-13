package com.maxar.target.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.common.exception.BadRequestException;
import com.maxar.target.exception.TargetQueryNoParametersException;
import com.maxar.target.model.BasTargetModel;
import com.maxar.target.model.DsaTargetModel;
import com.maxar.target.model.LocTargetModel;
import com.maxar.target.model.PointTargetModel;
import com.maxar.target.model.TargetModel;
import com.maxar.target.entity.BasTarget;
import com.maxar.target.entity.DsaTarget;
import com.maxar.target.entity.LocTarget;
import com.maxar.target.entity.PointTarget;
import com.maxar.target.entity.Target;
import com.maxar.target.repository.TargetRepository;

import scala.Console;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testtargetms.properties")
public class TargetControllerTest
{
	private static final String GOOD_COUNTRY_CODE = "TEST";
	private static final String BAD_COUNTRY_CODE = "XX";
	private static final String BAS_TARGET_ID = "B00001";
	private static final Long NUM_BAS_TARGETS_TEST = 1L; // our test BAS target
	private static final Long NUM_BAS_TARGETS = 10002L + NUM_BAS_TARGETS_TEST;
	private static final String DSA_TARGET_ID = "D00001";
	private static final Long NUM_DSA_TARGETS_TEST = 1L; // our test DSA target
	private static final Long NUM_DSA_TARGETS = 19968L + NUM_DSA_TARGETS_TEST;
	private static final String LOC_TARGET_ID = "L00001";
	private static final Long NUM_LOC_TARGETS_TEST = 1L; // our test LOC target
	private static final Long NUM_LOC_TARGETS = 10027L + NUM_LOC_TARGETS_TEST;
	private static final String POINT_TARGET_ID = "P000000001";
	private static final Long NUM_POINT_TARGETS_TEST = 1L; // our test point target
	private static final Long NUM_POINT_TARGETS = 160060L + NUM_POINT_TARGETS_TEST;
	private static final Long NUM_TARGETS_TEST = NUM_BAS_TARGETS_TEST + NUM_DSA_TARGETS_TEST + NUM_LOC_TARGETS_TEST
			+ NUM_POINT_TARGETS_TEST;
	private static final Long NUM_TARGETS = 255227L + NUM_TARGETS_TEST;
	private static final String GOOD_GEOMETRY = "POLYGON((24.63719 29.79381, 29.79381 29.79381, 29.79381 24.63719, 24.63719 24.63719, 24.63719 29.79381))";
	private static final String EMPTY_GEOMETRY = "POLYGON((0.0 0.0, 0.0 1.0, 1.0 1.0, 1.0 0.0, 0.0 0.0))";
	private static final String BAD_GEOMETRY = "PLYGON((24.63719 29.79381, 29.79381 29.79381, 29.79381 24.63719, 24.63719 24.63719, 24.63719 29.79381))";
	private static final String GOOD_EST_GEOMETRY = "POINT(20.0 20.0)";
	private static final String ESTIMATED_TARGET_NAME = "ESTIMATED TARGET";
	private static final String GOOD_CZML_START = "2020-01-01T00:00:00.000Z";
	private static final String GOOD_CZML_STOP = "2020-01-01T00:30:00.000Z";
	private static final String BAD_CZML_START = "2020-01-01X36:00:00.000Z";
	private static final String EXCEPTION_MESSAGE = "Target query must use at least one of following parameters: geometry, cc";

	// TODO: Replace the repositories with mock versions so that we don't have to
	// rely on local or 188 dbs to have static targets in them (i.e. if any targets
	// are added, removed or changed it could potentially break the unit tests)

	@Autowired
	TargetController targetController;

	@Autowired
	private TargetRepository targetRepository;

	private List<Target> testTargets;

	@Before
	public void setUp() {
		// TODO: If possible, we should populate the mock repos here

		// Create dummy targets for testing purposes
		testTargets = new ArrayList<>();

		final BasTarget bas = new BasTarget();
		bas.setTargetId("B_TEST");
		bas.setTargetName("BAS_UNIT_TEST");
		bas.setCountryCode(GOOD_COUNTRY_CODE);
		testTargets.add(bas);

		final DsaTarget dsa = new DsaTarget();
		dsa.setTargetId("D_TEST");
		dsa.setTargetName("DSA_UNIT_TEST");
		dsa.setCountryCode(GOOD_COUNTRY_CODE);
		testTargets.add(dsa);

		final LocTarget loc = new LocTarget();
		loc.setTargetId("L_TEST");
		loc.setTargetName("LOC_UNIT_TEST");
		loc.setCountryCode(GOOD_COUNTRY_CODE);
		testTargets.add(loc);

		final PointTarget pt = new PointTarget(
				"P_TEST", // ID
				"PT_UNIT_TEST", // Name
				10.0, // Lat
				10.0, // Lon
				0.0); // Alt
		pt.setCountryCode(GOOD_COUNTRY_CODE);
		testTargets.add(pt);

		targetRepository.saveAll(testTargets);
	}

	@After
	public void tearDown() {
		// TODO: If needed, we can clean up the mock repos here

		// Delete the dummy targets that we added for testing
		targetRepository.deleteAll(testTargets);
	}

	@Test
	public void contextLoads() {
		// code smell states to have at least 1 assertion
		Assert.assertTrue(true);
	}

	@Test
	public void testGetBasTargetsByCountryCode() {

		// test BAS w/ good CC
		ResponseEntity<List<BasTargetModel>> response = targetController.getBasTargetByCountryCode(	GOOD_COUNTRY_CODE,
																									0,
																									100);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertNotNull(response.getBody()
				.get(0));

		// test BAS w/ bad CC
		response = targetController.getBasTargetByCountryCode(	BAD_COUNTRY_CODE,
																0,
																100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
	}

	@Test
	public void testGetBasTargetById() {

		// test BAS w/ good ID
		ResponseEntity<BasTargetModel> response = targetController.getBasTargetById(BAS_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(BAS_TARGET_ID,
							response.getBody()
									.getTargetId());

		// test BAS w/ bad ID
		response = targetController.getBasTargetById(BAS_TARGET_ID + "x");

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NOT_FOUND,
							response.getStatusCode());
	}

	@Test
	public void testGetBasTargetCountCcNull() {
		final ResponseEntity<Long> response = targetController.getNumberOfBasTargets(null);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_BAS_TARGETS,
							response.getBody());
	}

	@Test
	public void testGetBasTargetCountCcEmpty() {
		final ResponseEntity<Long> response = targetController.getNumberOfBasTargets("");

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_BAS_TARGETS,
							response.getBody());
	}

	@Test
	public void testGetBasTargetCountCcGood() {
		final ResponseEntity<Long> response = targetController.getNumberOfBasTargets(GOOD_COUNTRY_CODE);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_BAS_TARGETS_TEST,
							response.getBody());
	}

	@Test
	public void testGetDsaTargetsByCountryCode() {

		// test DSA w/ good CC
		ResponseEntity<List<DsaTargetModel>> response = targetController.getDSATargetByCountryCode(	GOOD_COUNTRY_CODE,
																									0,
																									100);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertNotNull(response.getBody()
				.get(0));

		// test DSA w/ bad CC
		response = targetController.getDSATargetByCountryCode(	BAD_COUNTRY_CODE,
																0,
																100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
	}

	@Test
	public void testGetDsaTargetById() {

		// test DSA w/ good ID
		ResponseEntity<DsaTargetModel> response = targetController.getDSATargetById(DSA_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(DSA_TARGET_ID,
							response.getBody()
									.getTargetId());

		// test DSA w/ bad ID
		response = targetController.getDSATargetById(DSA_TARGET_ID + "x");

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NOT_FOUND,
							response.getStatusCode());
	}

	@Test
	public void testGetDsaTargetCountCcNull() {
		final ResponseEntity<Long> response = targetController.getNumberOfDSATargets(null);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_DSA_TARGETS,
							response.getBody());
	}

	@Test
	public void testGetDsaTargetCountCcEmpty() {
		final ResponseEntity<Long> response = targetController.getNumberOfDSATargets("");

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_DSA_TARGETS,
							response.getBody());
	}

	@Test
	public void testGetDsaTargetCountCcGood() {
		final ResponseEntity<Long> response = targetController.getNumberOfDSATargets(GOOD_COUNTRY_CODE);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_DSA_TARGETS_TEST,
							response.getBody());
	}

	@Test
	public void testGetLocTargetsByCountryCode() {

		// test LOC w/ good CC
		ResponseEntity<List<LocTargetModel>> response = targetController.getLOCTargetByCountryCode(	GOOD_COUNTRY_CODE,
																									0,
																									100);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertNotNull(response.getBody()
				.get(0));

		// test LOC w/ bad CC
		response = targetController.getLOCTargetByCountryCode(	BAD_COUNTRY_CODE,
																0,
																100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
	}

	@Test
	public void testGetLocTargetById() {

		// test LOC w/ good ID
		ResponseEntity<LocTargetModel> response = targetController.getLOCTargetById(LOC_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(LOC_TARGET_ID,
							response.getBody()
									.getTargetId());

		// test LOC w/ bad ID
		response = targetController.getLOCTargetById(LOC_TARGET_ID + "x");

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NOT_FOUND,
							response.getStatusCode());
	}

	@Test
	public void testGetLocTargetCountCcNull() {
		final ResponseEntity<Long> response = targetController.getNumberOfLOCTargets(null);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_LOC_TARGETS,
							response.getBody());
	}

	@Test
	public void testGetLocTargetCountCcEmpty() {
		final ResponseEntity<Long> response = targetController.getNumberOfLOCTargets("");

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_LOC_TARGETS,
							response.getBody());
	}

	@Test
	public void testGetLocTargetCountCcGood() {
		final ResponseEntity<Long> response = targetController.getNumberOfLOCTargets(GOOD_COUNTRY_CODE);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_LOC_TARGETS_TEST,
							response.getBody());
	}

	@Test
	public void testGetPointTargetsByCountryCode() {

		// test Point w/ good CC
		ResponseEntity<List<PointTargetModel>> response = targetController
				.getPointTargetByCountryCode(	GOOD_COUNTRY_CODE,
												0,
												100);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Console.out()
				.println("# of Points with CC=" + GOOD_COUNTRY_CODE + ": " + response.getBody()
						.size());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertNotNull(response.getBody()
				.get(0));

		// test Point w/ bad CC
		response = targetController.getPointTargetByCountryCode(BAD_COUNTRY_CODE,
																0,
																100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
	}

	@Test
	public void testGetPointTargetById() {

		// test Point w/ good ID
		ResponseEntity<PointTargetModel> response = targetController.getPointTargetById(POINT_TARGET_ID);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(POINT_TARGET_ID,
							response.getBody()
									.getTargetId());

		// test Point w/ bad ID
		response = targetController.getPointTargetById(POINT_TARGET_ID + "x");

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NOT_FOUND,
							response.getStatusCode());
	}

	@Test
	public void testGetPointTargetCountCcNull() {
		final ResponseEntity<Long> response = targetController.getNumberOfPointTargets(null);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_POINT_TARGETS,
							response.getBody());
	}

	@Test
	public void testGetPointTargetCountCcEmpty() {
		final ResponseEntity<Long> response = targetController.getNumberOfPointTargets("");

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_POINT_TARGETS,
							response.getBody());
	}

	@Test
	public void testGetPointTargetCountCcGood() {
		final ResponseEntity<Long> response = targetController.getNumberOfPointTargets(GOOD_COUNTRY_CODE);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_POINT_TARGETS_TEST,
							response.getBody());
	}

	@Test
	public void testGetTargetsByCountryCodeOrGeometry() {

		try {
			// test the exception condition of both parameters are null
			final ResponseEntity<List<TargetModel>> response = targetController.getTargets(	null,
																							null);
			Assert.assertNotNull(response);
			Assert.fail("This test should have thrown the TargetQueryNoParametersException!");
		}
		catch (final TargetQueryNoParametersException e) {
			Assert.assertEquals(EXCEPTION_MESSAGE,
								e.getMessage());
		}

		try {
			// Test geometry only
			ResponseEntity<List<TargetModel>> response = targetController.getTargets(	GOOD_GEOMETRY,
																						null);
			Assert.assertNotNull(response);
			Assert.assertNotNull(response.getBody());
			Assert.assertEquals(HttpStatus.OK,
								response.getStatusCode());
			Console.out()
					.println("# of Targets in 'GOOD_GEOMETRY' (null CCs): " + response.getBody()
							.size());
			Assert.assertEquals(38,
								response.getBody()
										.size());
			Assert.assertNotNull(response.getBody()
					.get(0));

			// Test bad geometry
			response = targetController.getTargets(	BAD_GEOMETRY,
													null);
			Assert.assertNotNull(response);
			Assert.assertEquals(HttpStatus.BAD_REQUEST,
								response.getStatusCode());

			// Test empty geometry
			response = targetController.getTargets(	EMPTY_GEOMETRY,
													null);
			Assert.assertNotNull(response);
			Assert.assertEquals(HttpStatus.NO_CONTENT,
								response.getStatusCode());

			// Test country code only
			response = targetController.getTargets(	null,
													Collections.singletonList("AA"));
			Assert.assertNotNull(response);
			Assert.assertNotNull(response.getBody());
			Assert.assertEquals(HttpStatus.OK,
								response.getStatusCode());
			Console.out()
					.println("# of Targets CC=AA (null geometry): " + response.getBody()
							.size());
			Assert.assertEquals(2,
								response.getBody()
										.size());
			Assert.assertNotNull(response.getBody()
					.get(0));

			// test geometry and country code
			response = targetController.getTargets(	GOOD_GEOMETRY,
													Collections.singletonList("EG"));
			Assert.assertNotNull(response);
			Assert.assertNotNull(response.getBody());
			Assert.assertEquals(HttpStatus.OK,
								response.getStatusCode());
			Console.out()
					.println("# of Targets in 'GOOD_GEOMETRY' and CC=EG: " + response.getBody()
							.size());
			Assert.assertEquals(25,
								response.getBody()
										.size());
			Assert.assertNotNull(response.getBody()
					.get(0));
		}
		catch (final TargetQueryNoParametersException e) {
			// We should never get here
			e.printStackTrace();
		}
	}

	@Test
	public void testGetTargetById() {

		// test target id only
		ResponseEntity<TargetModel> response = targetController.getTargetById(	POINT_TARGET_ID,
																				null,
																				null);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(POINT_TARGET_ID,
							response.getBody()
									.getTargetId());

		// test target id and czml start/stop
		response = targetController.getTargetById(	POINT_TARGET_ID,
													GOOD_CZML_START,
													GOOD_CZML_STOP);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(POINT_TARGET_ID,
							response.getBody()
									.getTargetId());
		Assert.assertEquals(GOOD_CZML_START,
							response.getBody()
									.getCzmlStartTime()
									.toDateTime(DateTimeZone.UTC)
									.toString());
		Assert.assertEquals(GOOD_CZML_STOP,
							response.getBody()
									.getCzmlStopTime()
									.toDateTime(DateTimeZone.UTC)
									.toString());

		// test target id and invalid czml start/stop
		response = targetController.getTargetById(	POINT_TARGET_ID,
													BAD_CZML_START,
													GOOD_CZML_STOP);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());

		// test invalid target id
		response = targetController.getTargetById(	POINT_TARGET_ID + "x",
													null,
													null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NOT_FOUND,
							response.getStatusCode());
	}

	@Test
	public void testGetTargetCountCcNull() {

		final ResponseEntity<Long> response = targetController.getNumberOfTargets(null);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_TARGETS,
							response.getBody());
	}

	@Test
	public void testGetTargetCountCcEmpty() {

		final ResponseEntity<Long> response = targetController.getNumberOfTargets("");

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_TARGETS,
							response.getBody());
	}

	@Test
	public void testGetTargetCountCcGood() {

		final ResponseEntity<Long> response = targetController.getNumberOfTargets(GOOD_COUNTRY_CODE);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(NUM_TARGETS_TEST,
							response.getBody());
	}

	@Test
	public void testGetEstimatedTarget() {

		// test geometry only
		ResponseEntity<TargetModel> response = targetController.getEstimatedTarget(	GOOD_EST_GEOMETRY,
																					null,
																					null);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(ESTIMATED_TARGET_NAME,
							response.getBody()
									.getTargetName());
		Assert.assertTrue(response.getBody()
				.isEstimated());
		Console.out()
				.println("Est. Target Geometry: " + response.getBody()
						.getGeometry()
						.toString());
		Assert.assertNull(response.getBody()
				.getCzmlStartTime());
		Assert.assertNull(response.getBody()
				.getCzmlStopTime());

		// test czml times and geometry
		response = targetController.getEstimatedTarget(	GOOD_EST_GEOMETRY,
														GOOD_CZML_START,
														GOOD_CZML_STOP);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(ESTIMATED_TARGET_NAME,
							response.getBody()
									.getTargetName());
		Assert.assertTrue(response.getBody()
				.isEstimated());
		Console.out()
				.println("Est. Target Geometry: " + response.getBody()
						.getGeometry()
						.toString());
		Console.out()
				.println("Est. Target Czml start-stop: " + response.getBody()
						.getCzmlStartTime()
						.toDateTime(DateTimeZone.UTC)
						.toString() + " - "
						+ response.getBody()
								.getCzmlStopTime()
								.toDateTime(DateTimeZone.UTC)
								.toString());
		Assert.assertEquals(GOOD_CZML_START,
							response.getBody()
									.getCzmlStartTime()
									.toDateTime(DateTimeZone.UTC)
									.toString());
		Assert.assertEquals(GOOD_CZML_STOP,
							response.getBody()
									.getCzmlStopTime()
									.toDateTime(DateTimeZone.UTC)
									.toString());
	}

	@Test
	public void testGetEstimatedTargetCzmlStartInvalid() {
		ResponseEntity<TargetModel> response = targetController.getEstimatedTarget(	GOOD_EST_GEOMETRY,
																					"invalid",
																					null);

		Assert.assertNotNull(response);
		Assert.assertNull(response.getBody());
		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
	}

	@Test
	public void testGetEstimatedTargetCzmlStopInvalid() {
		ResponseEntity<TargetModel> response = targetController.getEstimatedTarget(	GOOD_EST_GEOMETRY,
																					null,
																					"invalid");

		Assert.assertNotNull(response);
		Assert.assertNull(response.getBody());
		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
	}

	@Test
	public void testGetTargetsByCountryCode() {

		// test target w/ good CC
		ResponseEntity<List<TargetModel>> response = targetController.getTargetByCountryCode(	GOOD_COUNTRY_CODE,
																								0,
																								100);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Console.out()
				.println("# of Targets with CC=" + GOOD_COUNTRY_CODE + ": " + response.getBody()
						.size());
		Assert.assertEquals(4,
							response.getBody()
									.size());
		Assert.assertNotNull(response.getBody()
				.get(0));

		// test target w/ bad CC
		response = targetController.getTargetByCountryCode(	BAD_COUNTRY_CODE,
															0,
															100);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
	}

	@Test(expected = BadRequestException.class)
	public void testGetTargetsByCountryCodePageInvalid() {
		targetController.getTargetByCountryCode(GOOD_COUNTRY_CODE,
												-1,
												100);
	}

	@Test(expected = BadRequestException.class)
	public void testGetTargetsByCountryCodeCountInvalid() {
		targetController.getTargetByCountryCode(GOOD_COUNTRY_CODE,
												0,
												0);
	}

	@Test
	public void testGetTargetsByGeometry() {

		// test geometry only
		ResponseEntity<List<TargetModel>> response = targetController.getTargetsByGeometry(	GOOD_GEOMETRY,
																							false,
																							null,
																							null);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Console.out()
				.println("# of Targets in 'GOOD_GEOMETRY' (no est, null czml start/stop): " + response.getBody()
						.size());
		Assert.assertEquals(38,
							response.getBody()
									.size());
		Assert.assertNotNull(response.getBody()
				.get(0));

		// test bad geometry
		response = targetController.getTargetsByGeometry(	BAD_GEOMETRY,
															false,
															null,
															null);
		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());

		// test geometry and czml start/stop
		response = targetController.getTargetsByGeometry(	GOOD_GEOMETRY,
															false,
															GOOD_CZML_START,
															GOOD_CZML_STOP);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Console.out()
				.println("# of Targets in 'GOOD_GEOMETRY' (no est, with czml start/stop): " + response.getBody()
						.size());
		Assert.assertEquals(38,
							response.getBody()
									.size());
		Assert.assertNotNull(response.getBody()
				.get(0));

		// test geometry and bad czml start/stop
		response = targetController.getTargetsByGeometry(	GOOD_GEOMETRY,
															false,
															BAD_CZML_START,
															GOOD_CZML_STOP);
		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());

		// test empty geometry w/out generating estimate
		response = targetController.getTargetsByGeometry(	EMPTY_GEOMETRY,
															false,
															null,
															null);
		Assert.assertNotNull(response);
		Console.out()
				.println("'EMPTY_GEOMETRY' (no est, null czml start/stop) response code: " + response.getStatusCode());
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());

		// test empty geometry and generate estimate
		response = targetController.getTargetsByGeometry(	EMPTY_GEOMETRY,
															true,
															null,
															null);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Console.out()
				.println("# of Targets in 'EMPTY_GEOMETRY' (with est, null czml start/stop): " + response.getBody()
						.size());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertNotNull(response.getBody()
				.get(0));
		Console.out()
				.println("'EMPTY_GEOMETRY' (with est, null czml start/stop) target name: " + response.getBody()
						.get(0)
						.getTargetName());
		Console.out()
				.println("'EMPTY_GEOMETRY' (with est, null czml start/stop) estimated target: " + response.getBody()
						.get(0)
						.toString());
		Assert.assertEquals(ESTIMATED_TARGET_NAME,
							response.getBody()
									.get(0)
									.getTargetName());
	}

}
