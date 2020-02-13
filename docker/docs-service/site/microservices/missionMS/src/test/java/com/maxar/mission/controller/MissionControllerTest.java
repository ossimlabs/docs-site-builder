package com.maxar.mission.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.common.exception.BadRequestException;
import com.maxar.common.types.VehiclePosition;
import com.maxar.mission.model.MissionModel;
import com.maxar.mission.model.TrackModel;
import com.maxar.mission.entity.Mission;
import com.maxar.mission.entity.Track;
import com.maxar.mission.entity.TrackNode;
import com.maxar.mission.repository.MissionRepository;
import com.maxar.mission.repository.TrackRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testmissionms.properties")
public class MissionControllerTest
{
	private static final GeometryFactory geomFactory = new GeometryFactory();

	private static final String TRACK_ID = "TESTTRACK";

	private static final String BAD_TRACK_ID = "BADTESTTRACK";

	private static final String MISSION_ID = "TESTMISSION";

	private static final String BAD_MISSION_ID = "BADTESTMISSION";

	private static final String MISSION_ID_NO_TRACK = "TESTMISSION_NOTRACK";

	private static final String ASSET_ID = "1";

	private static final String NO_MISSION_ASSET_ID = "2";

	private static final String START_DATE = "2020-01-01T12:00:00Z";

	private static final String STOP_DATE = "2020-01-01T15:00:00Z";

	private static final String BAD_DATE = "BAD_DATE";

	private static final int GOOD_PAGE = 0;

	private static final int GOOD_COUNT = 100;

	private static final int BAD_PAGE = -1;

	private static final int BAD_COUNT = -1;

	private static final String SAMPLING_INTERVAL_MS = "60000";

	private static final String BAD_SAMPLING_INTERVAL_MS = "BAD";

	@MockBean
	TrackRepository trackRepository;

	@MockBean
	MissionRepository missionRepository;

	@Autowired
	MissionController missionController;

	@Before
	public void setUp() {
		final Track track = new Track();
		track.setId(TRACK_ID);
		track.setName(TRACK_ID);
		final List<TrackNode> nodes = new ArrayList<>();
		nodes.add(new TrackNode(
				track,
				1,
				getPoint(	0,
							0),
				0l,
				true));
		nodes.add(new TrackNode(
				track,
				2,
				getPoint(	0,
							15),
				7200000l,
				true));
		track.setTrackNodes(nodes);

		Mockito.when(trackRepository.findById(TRACK_ID))
				.thenReturn(track);

		Mockito.when(trackRepository.findById(BAD_TRACK_ID))
				.thenReturn(null);

		final Mission mission = new Mission();
		mission.setId(MISSION_ID);
		mission.setTrack(track);
		mission.setOnStationMillis(DateTime.parse(START_DATE)
				.getMillis());
		mission.setOffStationMillis(DateTime.parse(STOP_DATE)
				.getMillis());
		mission.setSpeedMPH(300.0);
		mission.setAltitudeMeters(1000.0);

		Mockito.when(missionRepository.findById(MISSION_ID))
				.thenReturn(mission);

		Mockito.when(missionRepository.findById(BAD_MISSION_ID))
				.thenReturn(null);

		final Mission missionNoTrack = new Mission();
		missionNoTrack.setId(MISSION_ID_NO_TRACK);
		Mockito.when(missionRepository.findById(MISSION_ID_NO_TRACK))
				.thenReturn(missionNoTrack);

		Mockito.when(missionRepository
				.findByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(	ArgumentMatchers
						.eq(ASSET_ID),
																											ArgumentMatchers
																													.anyLong(),
																											ArgumentMatchers
																													.anyLong()))
				.thenReturn(Collections.singletonList(mission));

		Mockito.when(missionRepository
				.findByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(	ArgumentMatchers
						.eq(NO_MISSION_ASSET_ID),
																											ArgumentMatchers
																													.anyLong(),
																											ArgumentMatchers
																													.anyLong()))
				.thenReturn(Collections.emptyList());

		final PageRequest pageRequest = PageRequest.of(	GOOD_PAGE,
														GOOD_COUNT,
														Sort.by("id"));
		Mockito.when(missionRepository
				.findByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(	ArgumentMatchers
						.eq(ASSET_ID),
																											ArgumentMatchers
																													.anyLong(),
																											ArgumentMatchers
																													.anyLong(),
																											ArgumentMatchers
																													.eq(pageRequest)))
				.thenReturn(Collections.singletonList(mission));

		Mockito.when(missionRepository
				.countByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(ArgumentMatchers
						.eq(ASSET_ID),
																											ArgumentMatchers
																													.anyLong(),
																											ArgumentMatchers
																													.anyLong()))
				.thenReturn(1l);
	}

	@Test
	public void testGetTrackById() {
		final ResponseEntity<TrackModel> response = missionController.getTrackById(TRACK_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		final TrackModel model = response.getBody();
		Assert.assertEquals(TRACK_ID,
							model.getId());
	}

	@Test
	public void testGetTrackByBadId() {
		final ResponseEntity<TrackModel> response = missionController.getTrackById(BAD_TRACK_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NOT_FOUND,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test
	public void testGetTrackByMissionId() {
		final ResponseEntity<TrackModel> response = missionController.getTrackByMissionId(MISSION_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		final TrackModel model = response.getBody();
		Assert.assertEquals(TRACK_ID,
							model.getId());
	}

	@Test
	public void testGetTrackByBadMissionId() {
		final ResponseEntity<TrackModel> response = missionController.getTrackByMissionId(BAD_MISSION_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NOT_FOUND,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test
	public void testGetTrackByMissionIdNoTrack() {
		final ResponseEntity<TrackModel> response = missionController.getTrackByMissionId(MISSION_ID_NO_TRACK);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test
	public void testGetMissionByAssetAndDate() {
		final ResponseEntity<List<MissionModel>> response = missionController.getMissionByAssetAndDate(	ASSET_ID,
																										START_DATE,
																										STOP_DATE);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		final List<MissionModel> models = response.getBody();
		Assert.assertEquals(1,
							models.size());
		Assert.assertEquals(MISSION_ID,
							models.get(0)
									.getId());
	}

	@Test(expected = BadRequestException.class)
	public void testGetMissionByAssetAndDateBadDate() {
		missionController.getMissionByAssetAndDate(	ASSET_ID,
													START_DATE,
													BAD_DATE);
	}

	@Test
	public void testGetMissionByAssetAndDateNoMissions() {
		final ResponseEntity<List<MissionModel>> response = missionController
				.getMissionByAssetAndDate(	NO_MISSION_ASSET_ID,
											START_DATE,
											STOP_DATE);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test
	public void testGetMissionByAssetAndDatePaged() {
		final ResponseEntity<List<MissionModel>> response = missionController.getMissionByAssetAndDatePaged(ASSET_ID,
																											START_DATE,
																											STOP_DATE,
																											GOOD_PAGE,
																											GOOD_COUNT);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		final List<MissionModel> models = response.getBody();
		Assert.assertEquals(1,
							models.size());
		Assert.assertEquals(MISSION_ID,
							models.get(0)
									.getId());
	}

	@Test(expected = BadRequestException.class)
	public void testGetMissionByAssetAndDatePagedBadPage() {
		missionController.getMissionByAssetAndDatePaged(ASSET_ID,
														START_DATE,
														STOP_DATE,
														BAD_PAGE,
														GOOD_COUNT);
	}

	@Test(expected = BadRequestException.class)
	public void testGetMissionByAssetAndDatePagedBadCount() {
		missionController.getMissionByAssetAndDatePaged(ASSET_ID,
														START_DATE,
														STOP_DATE,
														GOOD_PAGE,
														BAD_COUNT);
	}

	@Test
	public void testCountMissionByAssetAndDate() {
		final ResponseEntity<Long> response = missionController.countMissionByAssetAndDate(	ASSET_ID,
																							START_DATE,
																							STOP_DATE);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		final long count = response.getBody();
		Assert.assertEquals(1l,
							count);
	}

	@Test
	public void testGetPositions() {
		final ResponseEntity<List<VehiclePosition>> response = missionController.getPositions(	ASSET_ID,
																								START_DATE,
																								STOP_DATE,
																								SAMPLING_INTERVAL_MS);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		final List<VehiclePosition> positions = response.getBody();
		Assert.assertEquals(181,
							positions.size());
	}

	@Test
	public void testGetPositionsPaged() {
		final ResponseEntity<List<VehiclePosition>> response = missionController.getPositionsPaged(	ASSET_ID,
																									START_DATE,
																									STOP_DATE,
																									SAMPLING_INTERVAL_MS,
																									GOOD_PAGE,
																									GOOD_COUNT);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		final List<VehiclePosition> positions = response.getBody();
		Assert.assertEquals(181,
							positions.size());
	}

	@Test(expected = BadRequestException.class)
	public void testGetPositionsBadSamplingInterval() {
		missionController.getPositions(	ASSET_ID,
										START_DATE,
										STOP_DATE,
										BAD_SAMPLING_INTERVAL_MS);
	}

	private static Point getPoint(
			final double latitude,
			final double longitude ) {

		final Coordinate coord = new Coordinate(
				longitude,
				latitude);

		final Coordinate[] coordArray = {
			coord
		};

		final CoordinateArraySequence cas = new CoordinateArraySequence(
				coordArray);

		final Point point = new Point(
				cas,
				geomFactory);
		return point;
	}
}
