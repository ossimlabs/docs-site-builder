package com.maxar.mission.controller;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.asset.common.aircraft.AircraftTrackStateVectorProvider;
import com.maxar.common.czml.VehiclePositionCZMLUtils;
import com.maxar.common.exception.BadRequestException;
import com.maxar.common.types.VehiclePosition;
import com.maxar.mission.model.MissionModel;
import com.maxar.mission.model.TrackModel;
import com.maxar.mission.entity.Mission;
import com.maxar.mission.entity.Track;
import com.maxar.mission.repository.MissionRepository;
import com.maxar.mission.repository.TrackRepository;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.EarthCenteredFrame;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.mechanics.IStateVectorProvider;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "/missionMS")
public class MissionController
{
	private static Logger logger = SourceLogger.getLogger(MissionController.class.getName());

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	@Autowired
	private MissionRepository missionRepository;

	@Autowired
	private TrackRepository trackRepository;

	private static final String EXAMPLE_START_DATE = "2020-01-01T12:00:00Z";
	private static final String EXAMPLE_STOP_DATE = "2020-01-01T15:00:00Z";

	@GetMapping(value = "/tracks/id/{id}")
	@ApiOperation("Gets the Track for a given id")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The Track was successfully found"),
		@ApiResponse(code = 404, message = "No Track was found with given id")
	})
	public @ResponseBody ResponseEntity<TrackModel> getTrackById(
			@PathVariable
			@ApiParam(name = "id", value = "Track ID", example = "TRACK_1")
			final String id ) {
		final Track track = trackRepository.findById(id);

		if (track == null) {
			return ResponseEntity.notFound()
					.build();
		}

		final TrackModel model = track.toModel();

		return ResponseEntity.ok(model);
	}

	@GetMapping(value = "/tracks")
	@ApiOperation("Gets the Track for a given mission id")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The Track was successfully found"),
		@ApiResponse(code = 204, message = "The mission did not have a track"),
		@ApiResponse(code = 404, message = "No mission was found with given id")
	})
	public @ResponseBody ResponseEntity<TrackModel> getTrackByMissionId(
			@RequestParam
			@ApiParam(name = "missionId", value = "Mission ID", example = "MISSION_1")
			final String missionId ) {
		final Mission mission = missionRepository.findById(missionId);

		if (mission == null) {
			return ResponseEntity.notFound()
					.build();
		}

		if (mission.getTrack() == null) {
			return ResponseEntity.noContent()
					.build();
		}

		final TrackModel model = mission.getTrack()
				.toModel();

		return ResponseEntity.ok(model);
	}

	@GetMapping(value = "/missions")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The mission was successfully found"),
		@ApiResponse(code = 204, message = "No mission for asset and date range found"),
		@ApiResponse(code = 400, message = "The input ISO8601 dates could not be parsed")
	})
	public @ResponseBody ResponseEntity<List<MissionModel>> getMissionByAssetAndDate(
			@RequestParam
			@ApiParam(name = "assetId", value = "Asset ID to search for", example = "1")
			final String assetId,
			@RequestParam
			@ApiParam(name = "start", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_DATE)
			final String start,
			@RequestParam
			@ApiParam(name = "stop", value = "ISO8601 Formatted Date String", example = EXAMPLE_STOP_DATE)
			final String stop ) {
		final DateTime startTime = parseDateTimeString(start);
		final DateTime endTime = parseDateTimeString(stop);

		final List<Mission> missions = missionRepository
				.findByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(	assetId,
																											startTime
																													.getMillis(),
																											endTime.getMillis());

		return buildMissionListResponse(missions,
										assetId,
										start,
										stop);
	}

	@GetMapping(value = "/missions/paged")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The mission was successfully found"),
		@ApiResponse(code = 204, message = "No mission for asset and date range found"),
		@ApiResponse(code = 400, message = "The input ISO8601 dates could not be parsed, or the page or count "
				+ "parameters were invalid")
	})
	public @ResponseBody ResponseEntity<List<MissionModel>> getMissionByAssetAndDatePaged(
			@RequestParam
			@ApiParam(name = "assetId", value = "Asset ID to search for", example = "1")
			final String assetId,
			@RequestParam
			@ApiParam(name = "start", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_DATE)
			final String start,
			@RequestParam
			@ApiParam(name = "stop", value = "ISO8601 Formatted Date String", example = EXAMPLE_STOP_DATE)
			final String stop,
			@RequestParam
			@ApiParam(name = "page", value = "Page number", example = "0")
			final int page,
			@RequestParam
			@ApiParam(name = "count", value = "Number of results", example = "100")
			final int count ) {
		final DateTime startTime = parseDateTimeString(start);
		final DateTime endTime = parseDateTimeString(stop);
		validatePageAndCountParameters(	page,
										count);

		final PageRequest pageRequest = PageRequest.of(	page,
														count,
														Sort.by("id"));

		final List<Mission> missions = missionRepository
				.findByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(	assetId,
																											startTime
																													.getMillis(),
																											endTime.getMillis(),
																											pageRequest);

		return buildMissionListResponse(missions,
										assetId,
										start,
										stop);
	}

	@GetMapping("/missions/count")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The number of missions was successfully found"),
		@ApiResponse(code = 400, message = "The input ISO8601 dates could not be parsed")
	})
	public @ResponseBody ResponseEntity<Long> countMissionByAssetAndDate(
			@RequestParam
			@ApiParam(name = "assetId", value = "Asset ID", example = "1")
			final String assetId,
			@RequestParam
			@ApiParam(name = "startDateString", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_DATE)
			final String startDateString,
			@RequestParam
			@ApiParam(name = "endDateString", value = "ISO8601 Formatted Date String", example = EXAMPLE_STOP_DATE)
			final String endDateString ) {
		final DateTime start = parseDateTimeString(startDateString);
		final DateTime end = parseDateTimeString(endDateString);

		final long numMissions = missionRepository
				.countByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(assetId,
																											start.getMillis(),
																											end.getMillis());

		return ResponseEntity.ok(numMissions);
	}

	@GetMapping("/vehiclePositions")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The mission was successfully found"),
		@ApiResponse(code = 204, message = "No mission for asset and date range found"),
		@ApiResponse(code = 400, message = "The input ISO8601 dates could not be parsed, or the sampling interval could not be parsed")
	})
	public @ResponseBody ResponseEntity<List<VehiclePosition>> getPositions(
			@RequestParam
			@ApiParam(name = "assetId", value = "Asset ID", example = "1")
			final String assetId,
			@RequestParam
			@ApiParam(name = "startDateString", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_DATE)
			final String startDateString,
			@RequestParam
			@ApiParam(name = "endDateString", value = "ISO8601 Formatted Date String", example = EXAMPLE_STOP_DATE)
			final String endDateString,
			@RequestParam
			@ApiParam(name = "samplingInterval_ms", value = "Sampling Interval for position Data (ms)", example = "1000")
			final String samplingInterval_ms ) {
		final DateTime start = parseDateTimeString(startDateString);
		final DateTime end = parseDateTimeString(endDateString);
		final int samplingIntervalAsInteger = parseInteger(samplingInterval_ms);

		final List<Mission> missions = missionRepository
				.findByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(	assetId,
																											start.getMillis(),
																											end.getMillis());

		final List<VehiclePosition> positions = missions.stream()
				.map(mission -> calculatePositionsForMission(	mission,
																start,
																end,
																samplingIntervalAsInteger,
																assetId))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		return ResponseEntity.ok(positions);
	}

	@GetMapping("/vehiclePositions/paged")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The mission was successfully found"),
		@ApiResponse(code = 204, message = "No mission for asset and date range found"),
		@ApiResponse(code = 400, message = "The input ISO8601 dates could not be parsed, the sampling interval could "
				+ "not be parsed, or the page and count parameters were invalid")
	})
	public @ResponseBody ResponseEntity<List<VehiclePosition>> getPositionsPaged(
			@RequestParam
			@ApiParam(name = "assetId", value = "Asset ID", example = "1")
			final String assetId,
			@RequestParam
			@ApiParam(name = "startDateString", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_DATE)
			final String startDateString,
			@RequestParam
			@ApiParam(name = "endDateString", value = "ISO8601 Formatted Date String", example = EXAMPLE_STOP_DATE)
			final String endDateString,
			@RequestParam
			@ApiParam(name = "samplingInterval_ms", value = "Sampling Interval for position Data (ms)", example = "1000")
			final String samplingInterval_ms,
			@RequestParam
			@ApiParam(name = "page", value = "Page number", example = "0")
			final int page,
			@RequestParam
			@ApiParam(name = "count", value = "Number of results", example = "100")
			final int count ) {
		final DateTime start = parseDateTimeString(startDateString);
		final DateTime end = parseDateTimeString(endDateString);
		final int samplingIntervalAsInteger = parseInteger(samplingInterval_ms);
		validatePageAndCountParameters(	page,
										count);

		final PageRequest pageRequest = PageRequest.of(	page,
														count,
														Sort.by("id"));

		final List<Mission> missions = missionRepository
				.findByAssetIdIgnoreCaseAndOffStationMillisGreaterThanEqualAndOnStationMillisLessThanEqual(	assetId,
																											start.getMillis(),
																											end.getMillis(),
																											pageRequest);

		final List<VehiclePosition> positions = missions.stream()
				.map(mission -> calculatePositionsForMission(	mission,
																start,
																end,
																samplingIntervalAsInteger,
																assetId))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		return ResponseEntity.ok(positions);
	}

	/**
	 * Build a response entity with a list of missions, or a no content if there are
	 * no missions.
	 *
	 * @param missions
	 *            The list of missions to build the response with.
	 * @param assetId
	 *            The asset ID. This is only used if there are no missions.
	 * @param start
	 *            The start time. This is only used if there are no missions.
	 * @param stop
	 *            The end time. This is only used if there are no missions.
	 * @return An OK response entity with a list of missions if there are any
	 *         missions, or a no content if there are no missions.
	 */
	private ResponseEntity<List<MissionModel>> buildMissionListResponse(
			final List<Mission> missions,
			final String assetId,
			final String start,
			final String stop ) {
		if ((missions == null) || missions.isEmpty()) {
			logger.warn("No missions found for asset: " + assetId + " in date range: " + start + "/" + stop);
			return ResponseEntity.noContent()
					.build();
		}

		final List<MissionModel> missionModels = missions.stream()
				.map(Mission::toModel)
				.collect(Collectors.toList());

		return ResponseEntity.ok(missionModels);
	}

	/**
	 * Get all the vehicle positions for a mission and a time interval.
	 *
	 * @param mission
	 *            The mission for the asset.
	 * @param start
	 *            The start time of the interval for the vehicle positions.
	 * @param end
	 *            The end time of the interval for the vehicle positions.
	 * @param samplingIntervalMs
	 *            The sampling interval in milliseconds to calculate the vehicle
	 *            position for.
	 * @param assetId
	 *            The ID of the asset to calculate the positions for. This will be
	 *            set as the ID of each vehicle position object.
	 * @return A list of all positions of the vehicle during the interval requested,
	 *         based on the mission data and the sampling interval.
	 */
	private List<VehiclePosition> calculatePositionsForMission(
			final Mission mission,
			final DateTime start,
			final DateTime end,
			final int samplingIntervalMs,
			final String assetId ) {
		final IStateVectorProvider trackSvp = new AircraftTrackStateVectorProvider(
				mission.toModel());

		final List<StateVectorsInFrame> svifList = VehiclePositionCZMLUtils.generatePositionVectors(start,
																									end,
																									samplingIntervalMs,
																									trackSvp);

		final Length missionAltitude = Length.fromMeters(mission.getAltitudeMeters());

		return svifList.stream()
				.map(svif -> calculatePositionForFrame(	svif,
														assetId,
														missionAltitude))
				.collect(Collectors.toList());
	}

	/**
	 * Calculate the vehicle position for a single state vector in frame (SVIF).
	 *
	 * @param svif
	 *            The state vector in frame describing the vehicle's position.
	 * @param assetId
	 *            The ID of the asset. This will be set as the ID for the vehicle
	 *            position object.
	 * @param missionAltitude
	 *            The altitude of the mission.
	 * @return The vehicle position for the SVIF, including the altitude from the
	 *         mission.
	 */
	private VehiclePosition calculatePositionForFrame(
			final StateVectorsInFrame svif,
			final String assetId,
			final Length missionAltitude ) {
		final VehiclePosition vp = new VehiclePosition();
		vp.setId(assetId);
		final GeodeticPoint point = svif.geodeticPosition();
		final GeodeticPoint pointAtAltitude = GeodeticPoint.fromLatLonAlt(	point.latitude(),
																			point.longitude(),
																			missionAltitude);
		vp.setSvif(pointAtAltitude.getStateVectors(	svif.atTime(),
													EarthCenteredFrame.ECEF));

		return vp;
	}

	/**
	 * A wrapper for integer parsing that throws a BadRequestException if the
	 * integer string cannot be parsed.
	 *
	 * @param intString
	 *            The string that will be parsed into an integer.
	 * @return The integer that was parsed from the string.
	 */
	private static int parseInteger(
			final String intString ) {
		try {
			return Integer.parseInt(intString);
		}
		catch (final Exception e) {
			final String errorString = "Cannot convert " + intString + " to integer.";
			logger.error(errorString);
			throw new BadRequestException(
					errorString);
		}
	}

	/**
	 * Parse an ISO-8601 date time string, or throw a BadRequestException if the
	 * string cannot be parsed.
	 *
	 * @param dateTimeString
	 *            The string to parse, which should be in ISO-8601 format.
	 * @return A DateTime object parsed from the input string.
	 */
	private static DateTime parseDateTimeString(
			final String dateTimeString ) {
		try {
			return ISODateTimeFormat.dateTimeParser()
					.parseDateTime(dateTimeString);
		}
		catch (final Exception e) {
			final String errorString = "Cannot parse ISO8601 datetime String: " + dateTimeString;
			logger.error(errorString);
			throw new BadRequestException(
					errorString);
		}
	}

	/**
	 * Check the page and count request parameters.
	 *
	 * If the page number is less than 0, or if the count is less than or equal to
	 * 0, then a BadRequestException is thrown.
	 *
	 * @param page
	 *            The page number request parameter, which must be 0 or greater.
	 * @param count
	 *            The count request parameter, which must be greater than 0.
	 */
	private static void validatePageAndCountParameters(
			final int page,
			final int count ) {
		if (page < 0) {
			throw new BadRequestException(
					"Page request parameter must be >= 0");
		}
		if (count <= 0) {
			throw new BadRequestException(
					"Count request parameter must be > 0");
		}
	}
}
