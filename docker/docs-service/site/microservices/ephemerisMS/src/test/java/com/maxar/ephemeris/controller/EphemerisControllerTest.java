package com.maxar.ephemeris.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
import com.maxar.common.types.Vector3D;
import com.maxar.common.types.VehiclePosition;
import com.maxar.ephemeris.entity.StateVector;
import com.maxar.ephemeris.entity.StateVectorSet;
import com.maxar.ephemeris.entity.TLE;
import com.maxar.ephemeris.entity.VCM;
import com.maxar.ephemeris.model.EphemerisModel;
import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.ephemeris.model.StateVectorModel;
import com.maxar.ephemeris.model.StateVectorSetModel;
import com.maxar.ephemeris.model.TLEModel;
import com.maxar.ephemeris.model.VCMModel;
import com.maxar.ephemeris.repository.StateVectorSetRepository;
import com.maxar.ephemeris.repository.TLERepository;
import com.maxar.ephemeris.repository.VCMRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:testephemerisms.properties")
public class EphemerisControllerTest
{
	@Autowired
	private EphemerisController ephemerisController;

	@Autowired
	private TLERepository tleRepository;

	@Autowired
	private StateVectorSetRepository stateVectorSetRepository;

	@Autowired
	private VCMRepository vcmRepository;

	private static final int EXAMPLE_SCN = 32060;

	private static final String EXAMPLE_DESCRIPTION = "TEST TLE (WV-1)";

	private static final String EXAMPLE_TLE_LINE_1 = "1 32060U 07041A   19365.16240198  .00000495  00000-0  23031-4 0  9995";

	private static final String EXAMPLE_TLE_LINE_2 = "2 32060  97.4006  20.5953 0001275 111.0583 249.0791 15.24475338667774";

	private static final String EXAMPLE_TLE_START_DATE = "2020-01-01T12:00:00Z";

	private static final String EXAMPLE_TLE_END_DATE = "2020-01-01T13:00:00Z";

	private static final String EXAMPLE_SAMPLING_INTERVAL = "1000";

	// Making dates between state vector and tle far apart to guarantee model type
	// returned for testing purposes
	private static final String EXAMPLE_STATE_VECTOR_EPOCH_DATE = "2019-01-01T12:00:00Z";;

	private static final Long EXAMPLE_SV1_AT_TIME_MILLIS = 1546344000000L;

	private static final Double EXAMPLE_SV1_X_POS = -1609166.9529525214;
	private static final Double EXAMPLE_SV1_Y_POS = 884149.5805921489;
	private static final Double EXAMPLE_SV1_Z_POS = -6939032.899708027;

	private static final Double EXAMPLE_SV1_X_VEL = -2994.949034019323;
	private static final Double EXAMPLE_SV1_Y_VEL = 6642.288367747897;
	private static final Double EXAMPLE_SV1_Z_VEL = 1539.3798925236513;

	private static final Double EXAMPLE_SV1_X_ACCEL = 0.0;
	private static final Double EXAMPLE_SV1_Y_ACCEL = 0.0;
	private static final Double EXAMPLE_SV1_Z_ACCEL = 0.0;

	private static final Long EXAMPLE_SV2_AT_TIME_MILLIS = 1546344120000L;

	private static final Double EXAMPLE_SV2_X_POS = -1954811.120250542;
	private static final Double EXAMPLE_SV2_Y_POS = 1671353.49792403;
	private static final Double EXAMPLE_SV2_Z_POS = -6701344.703493115;

	private static final Double EXAMPLE_SV2_X_VEL = -2764.5992637523195;
	private static final Double EXAMPLE_SV2_Y_VEL = 6476.936425853835;
	private static final Double EXAMPLE_SV2_Z_VEL = 2421.597086097745;

	private static final Double EXAMPLE_SV2_X_ACCEL = 0.0;
	private static final Double EXAMPLE_SV2_Y_ACCEL = 0.0;
	private static final Double EXAMPLE_SV2_Z_ACCEL = 0.0;

	// VCM data is stored as an unstructured string so no need for real data
	private static final String EXAMPLE_VCM_DATA = "VCM example data string.";
	private static final String EXAMPLE_VCM_DATE = "2018-01-01T12:00:00Z";
	private static final Long EXAMPLE_VCM_DATE_MILLIS = 1514808000000L;

	@Before
	public void setUp() {
		final TLE tle = new TLE(
				EXAMPLE_SCN,
				EXAMPLE_DESCRIPTION,
				EXAMPLE_TLE_LINE_1,
				EXAMPLE_TLE_LINE_2);

		tleRepository.save(tle);

		final Vector3D sv1Pos = new Vector3D(
				EXAMPLE_SV1_X_POS,
				EXAMPLE_SV1_Y_POS,
				EXAMPLE_SV1_Z_POS);
		final Vector3D sv1Vel = new Vector3D(
				EXAMPLE_SV1_X_VEL,
				EXAMPLE_SV1_Y_VEL,
				EXAMPLE_SV1_Z_VEL);
		final Vector3D sv1Accel = new Vector3D(
				EXAMPLE_SV1_X_ACCEL,
				EXAMPLE_SV1_Y_ACCEL,
				EXAMPLE_SV1_Z_ACCEL);

		final StateVector sv1 = new StateVector(
				EXAMPLE_SV1_AT_TIME_MILLIS,
				sv1Pos,
				sv1Vel,
				sv1Accel,
				sv1Pos,
				sv1Vel,
				sv1Accel);

		final Vector3D sv2Pos = new Vector3D(
				EXAMPLE_SV2_X_POS,
				EXAMPLE_SV2_Y_POS,
				EXAMPLE_SV2_Z_POS);
		final Vector3D sv2Vel = new Vector3D(
				EXAMPLE_SV2_X_VEL,
				EXAMPLE_SV2_Y_VEL,
				EXAMPLE_SV2_Z_VEL);
		final Vector3D sv2Accel = new Vector3D(
				EXAMPLE_SV2_X_ACCEL,
				EXAMPLE_SV2_Y_ACCEL,
				EXAMPLE_SV2_Z_ACCEL);

		final StateVector sv2 = new StateVector(
				EXAMPLE_SV2_AT_TIME_MILLIS,
				sv2Pos,
				sv2Vel,
				sv2Accel,
				sv2Pos,
				sv2Vel,
				sv2Accel);

		final Set<StateVector> svList = new HashSet<>();

		svList.add(sv1);
		svList.add(sv2);

		final StateVectorSet svs = new StateVectorSet(
				EXAMPLE_SCN,
				svList);

		sv1.setStateVectorSet(svs);
		sv2.setStateVectorSet(svs);

		stateVectorSetRepository.save(svs);

		final VCM vcm = new VCM(
				EXAMPLE_SCN,
				EXAMPLE_VCM_DATE_MILLIS,
				EXAMPLE_VCM_DATA);

		vcmRepository.save(vcm);
	}

	@After
	public void tearDown() {
		tleRepository.deleteAll();
		stateVectorSetRepository.deleteAll();
		vcmRepository.deleteAll();
	}

	@Test
	public void testgetEphemerisTLE() {
		final ResponseEntity<EphemerisModel> response = ephemerisController.getEphemeris(	String.valueOf(EXAMPLE_SCN),
																							EXAMPLE_TLE_START_DATE);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());

		Assert.assertTrue(response.getBody() instanceof TLEModel);

		final TLEModel tleModel = (TLEModel) response.getBody();

		Assert.assertEquals(EphemerisType.TLE,
							tleModel.getType());
		Assert.assertEquals(EXAMPLE_SCN,
							tleModel.getScn());
		Assert.assertEquals(EXAMPLE_DESCRIPTION,
							tleModel.getDescription());
		Assert.assertEquals(EXAMPLE_TLE_LINE_1,
							tleModel.getTleLineOne());
		Assert.assertEquals(EXAMPLE_TLE_LINE_2,
							tleModel.getTleLineTwo());
	}

	@Test
	public void testgetEphemerisStateVectorSet() {
		final ResponseEntity<EphemerisModel> response = ephemerisController.getEphemeris(	String.valueOf(EXAMPLE_SCN),
																							EXAMPLE_STATE_VECTOR_EPOCH_DATE);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());

		Assert.assertTrue(response.getBody() instanceof StateVectorSetModel);

		final StateVectorSetModel svsModel = (StateVectorSetModel) response.getBody();

		Assert.assertEquals(EphemerisType.STATE_VECTOR_SET,
							svsModel.getType());
		Assert.assertEquals(EXAMPLE_SCN,
							svsModel.getScn());
		Assert.assertEquals(2,
							svsModel.getStateVectors()
									.size());

		final List<StateVectorModel> svList = new ArrayList<>();
		svList.addAll(svsModel.getStateVectors());

		svList.sort(Comparator.comparing(StateVectorModel::getAtTimeMillis));

		final StateVectorModel sv1Model = svList.get(0);

		Assert.assertEquals(EXAMPLE_SV1_AT_TIME_MILLIS,
							(Long) sv1Model.getAtTimeMillis());
		Assert.assertEquals(EXAMPLE_SV1_X_POS,
							(Double) sv1Model.getEcfPos()
									.getX());
		Assert.assertEquals(EXAMPLE_SV1_Y_POS,
							(Double) sv1Model.getEcfPos()
									.getY());
		Assert.assertEquals(EXAMPLE_SV1_Z_POS,
							(Double) sv1Model.getEcfPos()
									.getZ());
		Assert.assertEquals(EXAMPLE_SV1_X_VEL,
							(Double) sv1Model.getEcfVel()
									.getX());
		Assert.assertEquals(EXAMPLE_SV1_Y_VEL,
							(Double) sv1Model.getEcfVel()
									.getY());
		Assert.assertEquals(EXAMPLE_SV1_Z_VEL,
							(Double) sv1Model.getEcfVel()
									.getZ());
		Assert.assertEquals(EXAMPLE_SV1_X_ACCEL,
							(Double) sv1Model.getEcfAccel()
									.getX());
		Assert.assertEquals(EXAMPLE_SV1_Y_ACCEL,
							(Double) sv1Model.getEcfAccel()
									.getY());
		Assert.assertEquals(EXAMPLE_SV1_Z_ACCEL,
							(Double) sv1Model.getEcfAccel()
									.getZ());
		Assert.assertEquals(EXAMPLE_SV1_X_POS,
							(Double) sv1Model.getEciPos()
									.getX());
		Assert.assertEquals(EXAMPLE_SV1_Y_POS,
							(Double) sv1Model.getEciPos()
									.getY());
		Assert.assertEquals(EXAMPLE_SV1_Z_POS,
							(Double) sv1Model.getEciPos()
									.getZ());
		Assert.assertEquals(EXAMPLE_SV1_X_VEL,
							(Double) sv1Model.getEciVel()
									.getX());
		Assert.assertEquals(EXAMPLE_SV1_Y_VEL,
							(Double) sv1Model.getEciVel()
									.getY());
		Assert.assertEquals(EXAMPLE_SV1_Z_VEL,
							(Double) sv1Model.getEciVel()
									.getZ());
		Assert.assertEquals(EXAMPLE_SV1_X_ACCEL,
							(Double) sv1Model.getEciAccel()
									.getX());
		Assert.assertEquals(EXAMPLE_SV1_Y_ACCEL,
							(Double) sv1Model.getEciAccel()
									.getY());
		Assert.assertEquals(EXAMPLE_SV1_Z_ACCEL,
							(Double) sv1Model.getEciAccel()
									.getZ());

		final StateVectorModel sv2Model = svList.get(1);

		Assert.assertEquals(EXAMPLE_SV2_AT_TIME_MILLIS,
							(Long) sv2Model.getAtTimeMillis());
		Assert.assertEquals(EXAMPLE_SV2_X_POS,
							(Double) sv2Model.getEcfPos()
									.getX());
		Assert.assertEquals(EXAMPLE_SV2_Y_POS,
							(Double) sv2Model.getEcfPos()
									.getY());
		Assert.assertEquals(EXAMPLE_SV2_Z_POS,
							(Double) sv2Model.getEcfPos()
									.getZ());
		Assert.assertEquals(EXAMPLE_SV2_X_VEL,
							(Double) sv2Model.getEcfVel()
									.getX());
		Assert.assertEquals(EXAMPLE_SV2_Y_VEL,
							(Double) sv2Model.getEcfVel()
									.getY());
		Assert.assertEquals(EXAMPLE_SV2_Z_VEL,
							(Double) sv2Model.getEcfVel()
									.getZ());
		Assert.assertEquals(EXAMPLE_SV2_X_ACCEL,
							(Double) sv2Model.getEcfAccel()
									.getX());
		Assert.assertEquals(EXAMPLE_SV2_Y_ACCEL,
							(Double) sv2Model.getEcfAccel()
									.getY());
		Assert.assertEquals(EXAMPLE_SV2_Z_ACCEL,
							(Double) sv2Model.getEcfAccel()
									.getZ());
		Assert.assertEquals(EXAMPLE_SV2_X_POS,
							(Double) sv2Model.getEciPos()
									.getX());
		Assert.assertEquals(EXAMPLE_SV2_Y_POS,
							(Double) sv2Model.getEciPos()
									.getY());
		Assert.assertEquals(EXAMPLE_SV2_Z_POS,
							(Double) sv2Model.getEciPos()
									.getZ());
		Assert.assertEquals(EXAMPLE_SV2_X_VEL,
							(Double) sv2Model.getEciVel()
									.getX());
		Assert.assertEquals(EXAMPLE_SV2_Y_VEL,
							(Double) sv2Model.getEciVel()
									.getY());
		Assert.assertEquals(EXAMPLE_SV2_Z_VEL,
							(Double) sv2Model.getEciVel()
									.getZ());
		Assert.assertEquals(EXAMPLE_SV2_X_ACCEL,
							(Double) sv2Model.getEciAccel()
									.getX());
		Assert.assertEquals(EXAMPLE_SV2_Y_ACCEL,
							(Double) sv2Model.getEciAccel()
									.getY());
		Assert.assertEquals(EXAMPLE_SV2_Z_ACCEL,
							(Double) sv2Model.getEciAccel()
									.getZ());
	}

	@Test
	public void testgetEphemerisVCM() {
		final ResponseEntity<EphemerisModel> response = ephemerisController.getEphemeris(	String.valueOf(EXAMPLE_SCN),
																							EXAMPLE_VCM_DATE);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());

		Assert.assertTrue(response.getBody() instanceof VCMModel);

		final VCMModel vcmModel = (VCMModel) response.getBody();

		Assert.assertEquals(EphemerisType.VCM,
							vcmModel.getType());
		Assert.assertEquals(EXAMPLE_SCN,
							vcmModel.getScn());
		Assert.assertEquals(EXAMPLE_VCM_DATA,
							vcmModel.getVcm());
		Assert.assertEquals(EXAMPLE_VCM_DATE_MILLIS,
							(Long) vcmModel.getEpochMillis());
	}

	@Test
	public void testgetEphemerisDateNull() {
		final ResponseEntity<EphemerisModel> response = ephemerisController.getEphemeris(	String.valueOf(EXAMPLE_SCN),
																							null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());

		final TLEModel tleModel = (TLEModel) response.getBody();

		Assert.assertEquals(EphemerisType.TLE,
							tleModel.getType());
		Assert.assertEquals(EXAMPLE_SCN,
							tleModel.getScn());
		Assert.assertEquals(EXAMPLE_DESCRIPTION,
							tleModel.getDescription());
		Assert.assertEquals(EXAMPLE_TLE_LINE_1,
							tleModel.getTleLineOne());
		Assert.assertEquals(EXAMPLE_TLE_LINE_2,
							tleModel.getTleLineTwo());
	}

	@Test
	public void testgetEphemerisScnNoMatch() {
		final ResponseEntity<EphemerisModel> response = ephemerisController.getEphemeris(	"1",
																							EXAMPLE_TLE_START_DATE);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test
	public void testgetEphemerisDateNoMatch() {
		final ResponseEntity<EphemerisModel> response = ephemerisController.getEphemeris(	String.valueOf(EXAMPLE_SCN),
																							"1990-01-01T00:00:00Z");

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = BadRequestException.class)
	public void testgetEphemerisScnNull() {
		ephemerisController.getEphemeris(	null,
											EXAMPLE_TLE_START_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testgetEphemerisScnEmpty() {
		ephemerisController.getEphemeris(	"",
											EXAMPLE_TLE_START_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testgetEphemerisScnInvalid() {
		ephemerisController.getEphemeris(	"z",
											EXAMPLE_TLE_START_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testgetEphemerisDateEmpty() {
		ephemerisController.getEphemeris(	String.valueOf(EXAMPLE_SCN),
											"");
	}

	@Test(expected = BadRequestException.class)
	public void testgetEphemerisDateInvalid() {
		ephemerisController.getEphemeris(	String.valueOf(EXAMPLE_SCN),
											"abc");
	}

	@Test
	public void testGetPositions() {
		final ResponseEntity<List<VehiclePosition>> response = ephemerisController
				.getPositions(	String.valueOf(EXAMPLE_SCN),
								EXAMPLE_TLE_START_DATE,
								EXAMPLE_TLE_END_DATE,
								EXAMPLE_SAMPLING_INTERVAL);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());

		final List<VehiclePosition> vehiclePositions = response.getBody();

		Assert.assertEquals(3601,
							vehiclePositions.size());
		Assert.assertTrue(vehiclePositions.stream()
				.map(VehiclePosition::getId)
				.allMatch(String.valueOf(EXAMPLE_SCN)::equals));
		Assert.assertTrue(vehiclePositions.stream()
				.map(VehiclePosition::getSvif)
				.allMatch(Objects::nonNull));
	}

	@Test
	public void testGetPositionsJ2Propagator() {
		// Use some dates sufficiently past the TLE date to trigger the J2 propagator
		// logic.
		final ResponseEntity<List<VehiclePosition>> response = ephemerisController
				.getPositions(	String.valueOf(EXAMPLE_SCN),
								"2020-01-11T12:00:00Z",
								"2020-01-11T13:00:00Z",
								EXAMPLE_SAMPLING_INTERVAL);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());

		final List<VehiclePosition> vehiclePositions = response.getBody();

		Assert.assertEquals(3601,
							vehiclePositions.size());
		Assert.assertTrue(vehiclePositions.stream()
				.map(VehiclePosition::getId)
				.allMatch(String.valueOf(EXAMPLE_SCN)::equals));
		Assert.assertTrue(vehiclePositions.stream()
				.map(VehiclePosition::getSvif)
				.allMatch(Objects::nonNull));
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsScnNull() {
		ephemerisController.getPositions(	null,
											EXAMPLE_TLE_START_DATE,
											EXAMPLE_TLE_END_DATE,
											EXAMPLE_SAMPLING_INTERVAL);
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsScnEmpty() {
		ephemerisController.getPositions(	"",
											EXAMPLE_TLE_START_DATE,
											EXAMPLE_TLE_END_DATE,
											EXAMPLE_SAMPLING_INTERVAL);
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsScnInvalid() {
		ephemerisController.getPositions(	"x",
											EXAMPLE_TLE_START_DATE,
											EXAMPLE_TLE_END_DATE,
											EXAMPLE_SAMPLING_INTERVAL);
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsStartDateNull() {
		ephemerisController.getPositions(	String.valueOf(EXAMPLE_SCN),
											null,
											EXAMPLE_TLE_END_DATE,
											EXAMPLE_SAMPLING_INTERVAL);
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsStartDateEmpty() {
		ephemerisController.getPositions(	String.valueOf(EXAMPLE_SCN),
											"",
											EXAMPLE_TLE_END_DATE,
											EXAMPLE_SAMPLING_INTERVAL);
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsStartDateInvalid() {
		ephemerisController.getPositions(	String.valueOf(EXAMPLE_SCN),
											"asdf",
											EXAMPLE_TLE_END_DATE,
											EXAMPLE_SAMPLING_INTERVAL);
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsEndDateNull() {
		ephemerisController.getPositions(	String.valueOf(EXAMPLE_SCN),
											EXAMPLE_TLE_START_DATE,
											null,
											EXAMPLE_SAMPLING_INTERVAL);
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsEndDateEmpty() {
		ephemerisController.getPositions(	String.valueOf(EXAMPLE_SCN),
											EXAMPLE_TLE_START_DATE,
											"",
											EXAMPLE_SAMPLING_INTERVAL);
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsEndDateInvalid() {
		ephemerisController.getPositions(	String.valueOf(EXAMPLE_SCN),
											EXAMPLE_TLE_START_DATE,
											"asdf",
											EXAMPLE_SAMPLING_INTERVAL);
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsSamplingIntervalNull() {
		ephemerisController.getPositions(	String.valueOf(EXAMPLE_SCN),
											EXAMPLE_TLE_START_DATE,
											EXAMPLE_TLE_END_DATE,
											null);
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsSamplingIntervalEmpty() {
		ephemerisController.getPositions(	String.valueOf(EXAMPLE_SCN),
											EXAMPLE_TLE_START_DATE,
											EXAMPLE_TLE_END_DATE,
											"");
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsSamplingIntervalInvalid() {
		ephemerisController.getPositions(	String.valueOf(EXAMPLE_SCN),
											EXAMPLE_TLE_START_DATE,
											EXAMPLE_TLE_END_DATE,
											"a");
	}
}
