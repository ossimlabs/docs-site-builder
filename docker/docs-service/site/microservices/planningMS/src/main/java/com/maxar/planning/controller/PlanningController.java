package com.maxar.planning.controller;

import static com.maxar.common.utils.PaginationParameterValidator.validatePageAndCountParameters;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.common.exception.BadRequestException;
import com.maxar.planning.entity.image.CollectionWindow;
import com.maxar.planning.entity.tasking.Tasking;
import com.maxar.planning.exception.PlanningQueryNoParametersException;
import com.maxar.planning.model.image.CollectionWindowModel;
import com.maxar.planning.model.tasking.TaskingModel;
import com.maxar.planning.repository.CollectionWindowRepository;
import com.maxar.planning.repository.TaskingRepository;
import com.radiantblue.analytics.core.log.SourceLogger;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "/planningMS")
public class PlanningController
{
	private static Logger logger = SourceLogger.getLogger(PlanningController.class.getName());

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	// Static strings for swagger examples
	private static final String EXAMPLE_MISSION_ID = "MISSION_16";
	private static final String EXAMPLE_CW_ID = "CW: 2";
	private static final String EXAMPLE_TARGET_ID = "P000077994";
	private static final String EXAMPLE_ASSET_NAME = "CSM3";
	private static final String EXAMPLE_ASSET_SCN = "33412";
	private static final String EXAMPLE_START_TASKING = "2020-01-01T11:00:00";
	private static final String EXAMPLE_END_TASKING = "2020-01-01T12:00:00";
	private static final String EXAMPLE_START_CW = "2020-01-01T03:00:00";
	private static final String EXAMPLE_STOP_CW = "2020-01-01T04:00:00";

	@Autowired
	private CollectionWindowRepository cwRepository;

	@Autowired
	private TaskingRepository taskingRepository;

	@GetMapping(value = "/tasking")
	@ApiOperation("Gets the taskings for a given mission and date range")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The taskings were successfully found"),
		@ApiResponse(code = 204, message = "No taskings were found for given parameters"),
		@ApiResponse(code = 400, message = "The input IS08601 dates could not be parsed, or not enough query parameters were specified")
	})
	public @ResponseBody ResponseEntity<List<TaskingModel>> getTasking(
			@RequestParam(required = false)
			@ApiParam(name = "missionId", value = "Mission ID", example = EXAMPLE_MISSION_ID)
			final String missionId,
			@RequestParam(required = false)
			@ApiParam(name = "start", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_TASKING)
			final String start,
			@RequestParam(required = false)
			@ApiParam(name = "stop", value = "ISO8601 Formatted Date String", example = EXAMPLE_END_TASKING)
			final String stop,
			@RequestParam(required = false)
			@ApiParam(name = "targetId", value = "Target ID", example = EXAMPLE_TARGET_ID)
			final String targetId )
			throws PlanningQueryNoParametersException {
		validateTaskingParameters(	missionId,
									targetId);

		final DateTime startTime = parseDateTimeString(start);
		final DateTime stopTime = parseDateTimeString(stop);

		final List<Tasking> taskings = taskingRepository.findTasking(	missionId,
																		(startTime == null) ? null
																				: startTime.getMillis(),
																		(stopTime == null) ? null
																				: stopTime.getMillis(),
																		targetId);

		if ((taskings == null) || taskings.isEmpty()) {
			logger.warn("No taskings found for date range: " + start + "/" + stop + " or mission ID: " + missionId);
			return ResponseEntity.noContent()
					.build();
		}

		final List<TaskingModel> models = taskings.stream()
				.map(Tasking::toModel)
				.collect(Collectors.toList());

		return ResponseEntity.ok(models);
	}

	@GetMapping(value = "/tasking/count")
	@ApiOperation("Gets the number of taskings for a given mission and date range")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The number of taskings were successfully found"),
		@ApiResponse(code = 400, message = "The input IS08601 dates could not be parsed, or not enough query parameters were specified")
	})
	public @ResponseBody ResponseEntity<Long> countTasking(
			@RequestParam(required = false)
			@ApiParam(name = "missionId", value = "Mission ID", example = EXAMPLE_MISSION_ID)
			final String missionId,
			@RequestParam(required = false)
			@ApiParam(name = "start", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_TASKING)
			final String start,
			@RequestParam(required = false)
			@ApiParam(name = "stop", value = "ISO8601 Formatted Date String", example = EXAMPLE_END_TASKING)
			final String stop,
			@RequestParam(required = false)
			@ApiParam(name = "targetId", value = "Target ID", example = EXAMPLE_TARGET_ID)
			final String targetId )
			throws PlanningQueryNoParametersException {
		validateTaskingParameters(	missionId,
									targetId);

		final DateTime startTime = parseDateTimeString(start);
		final DateTime stopTime = parseDateTimeString(stop);

		final long numTaskings = taskingRepository.countTasking(missionId,
																(startTime == null) ? null : startTime.getMillis(),
																(stopTime == null) ? null : stopTime.getMillis(),
																targetId);

		return ResponseEntity.ok(numTaskings);
	}

	@GetMapping(value = "/tasking/paged")
	@ApiOperation("Gets the taskings for a given mission and date range")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The taskings were successfully found"),
		@ApiResponse(code = 204, message = "No taskings were found for given parameters"),
		@ApiResponse(code = 400, message = "The input IS08601 dates could not be parsed, or not enough query parameters were specified")
	})
	public @ResponseBody ResponseEntity<List<TaskingModel>> getTaskingPaged(
			@RequestParam(required = false)
			@ApiParam(name = "missionId", value = "Mission ID", example = EXAMPLE_MISSION_ID)
			final String missionId,
			@RequestParam(required = false)
			@ApiParam(name = "start", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_TASKING)
			final String start,
			@RequestParam(required = false)
			@ApiParam(name = "stop", value = "ISO8601 Formatted Date String", example = EXAMPLE_END_TASKING)
			final String stop,
			@RequestParam(required = false)
			@ApiParam(name = "targetId", value = "Target ID", example = EXAMPLE_TARGET_ID)
			final String targetId,
			@RequestParam
			@ApiParam(name = "page", value = "Page number", example = "0")
			final int page,
			@RequestParam
			@ApiParam(name = "count", value = "Number of results", example = "100")
			final int count )
			throws PlanningQueryNoParametersException {
		validateTaskingParameters(	missionId,
									targetId);

		validatePageAndCountParameters(	page,
										count);

		final DateTime startTime = parseDateTimeString(start);
		final DateTime stopTime = parseDateTimeString(stop);

		final PageRequest pageRequest = PageRequest.of(	page,
														count,
														Sort.by("missionId"));

		final List<Tasking> taskings = taskingRepository.findTaskingPaged(	missionId,
																			(startTime == null) ? null
																					: startTime.getMillis(),
																			(stopTime == null) ? null
																					: stopTime.getMillis(),
																			targetId,
																			pageRequest);

		if ((taskings == null) || taskings.isEmpty()) {
			logger.warn("No taskings found for date range: " + start + "/" + stop + " or mission ID: " + missionId);
			return ResponseEntity.noContent()
					.build();
		}

		final List<TaskingModel> models = taskings.stream()
				.map(Tasking::toModel)
				.collect(Collectors.toList());

		return ResponseEntity.ok(models);
	}

	@GetMapping(value = "/collectionWindows")
	@ApiOperation("Gets the collection windows for a given list of parameters")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The collection windows were successfully found"),
		@ApiResponse(code = 204, message = "No collection windows were found for given parameters"),
		@ApiResponse(code = 400, message = "The input IS08601 dates could not be parsed, or not enough query parameters were specified")
	})
	@Transactional
	public @ResponseBody ResponseEntity<List<CollectionWindowModel>> getCollectionWindows(
			@RequestParam(required = false)
			@ApiParam(name = "name", value = "Asset Name", example = EXAMPLE_ASSET_NAME)
			final String name,
			@RequestParam(required = false)
			@ApiParam(name = "scn", value = "Asset SCN", example = EXAMPLE_ASSET_SCN)
			final Integer scn,
			@RequestParam(required = false)
			@ApiParam(name = "start", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_CW)
			final String start,
			@RequestParam(required = false)
			@ApiParam(name = "stop", value = "ISO8601 Formatted Date String", example = EXAMPLE_STOP_CW)
			final String stop,
			@RequestParam(required = false)
			@ApiParam(name = "targetId", value = "Target ID", example = EXAMPLE_TARGET_ID)
			final String targetId )
			throws PlanningQueryNoParametersException {
		validateCollectionWindowParameters(	name,
											scn,
											start,
											stop);

		final DateTime startTime = parseDateTimeString(start);
		final DateTime stopTime = parseDateTimeString(stop);

		final List<CollectionWindow> cws = cwRepository.findCollectionWindows(	scn,
																				name,
																				(startTime == null) ? null
																						: startTime.getMillis(),
																				(stopTime == null) ? null
																						: stopTime.getMillis(),
																				targetId);

		return formCollectionWindowsResponse(	cws,
												start,
												stop,
												scn,
												name);
	}

	@GetMapping(value = "/collectionWindow")
	@ApiOperation("Gets a single collection window for a given list of parameters and a collection window ID")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The collection window was successfully found"),
		@ApiResponse(code = 204, message = "No collection window was found for given parameters"),
		@ApiResponse(code = 400, message = "The input IS08601 dates could not be parsed, or not enough query parameters were specified")
	})
	@Transactional
	public @ResponseBody ResponseEntity<CollectionWindowModel> getCollectionWindow(
			@RequestParam
			@ApiParam(name = "cwId", value = "Collection Window ID", example = EXAMPLE_CW_ID)
			final String cwId,
			@RequestParam(required = false)
			@ApiParam(name = "name", value = "Asset Name", example = EXAMPLE_ASSET_NAME)
			final String name,
			@RequestParam(required = false)
			@ApiParam(name = "scn", value = "Asset SCN", example = EXAMPLE_ASSET_SCN)
			final Integer scn,
			@RequestParam(required = false)
			@ApiParam(name = "start", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_CW)
			final String start,
			@RequestParam(required = false)
			@ApiParam(name = "stop", value = "ISO8601 Formatted Date String", example = EXAMPLE_STOP_CW)
			final String stop,
			@RequestParam(required = false)
			@ApiParam(name = "targetId", value = "Target ID", example = EXAMPLE_TARGET_ID)
			final String targetId )
			throws PlanningQueryNoParametersException {
		validateCollectionWindowParameters(	name,
											scn,
											start,
											stop);

		final DateTime startTime = parseDateTimeString(start);
		final DateTime stopTime = parseDateTimeString(stop);

		final List<CollectionWindow> cws = cwRepository.findCollectionWindows(	scn,
																				name,
																				(startTime == null) ? null
																						: startTime.getMillis(),
																				(stopTime == null) ? null
																						: stopTime.getMillis(),
																				targetId);

		CollectionWindowModel model = null;
		for (final CollectionWindow cw : cws) {
			if (cw.getCwId()
					.equals(cwId)) {
				model = cw.toModel();
				break;
			}
		}

		if (model == null) {
			logger.warn("No collection window found for date range: " + start + "/" + stop + " asset: " + scn + "/"
					+ name + "/" + " cwId: " + cwId);
			return ResponseEntity.noContent()
					.build();
		}

		return ResponseEntity.ok(model);
	}

	@GetMapping(value = "/collectionWindows/paged")
	@ApiOperation("Gets the collection windows for a given list of parameters")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The collection windows were successfully found"),
		@ApiResponse(code = 204, message = "No collection windows were found for given parameters"),
		@ApiResponse(code = 400, message = "The input IS08601 dates could not be parsed, or not enough query parameters were specified")
	})
	@Transactional
	public @ResponseBody ResponseEntity<List<CollectionWindowModel>> getCollectionWindowsPaged(
			@RequestParam(required = false)
			@ApiParam(name = "name", value = "Asset Name", example = EXAMPLE_ASSET_NAME)
			final String name,
			@RequestParam(required = false)
			@ApiParam(name = "scn", value = "Asset SCN", example = EXAMPLE_ASSET_SCN)
			final Integer scn,
			@RequestParam(required = false)
			@ApiParam(name = "start", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_CW)
			final String start,
			@RequestParam(required = false)
			@ApiParam(name = "stop", value = "ISO8601 Formatted Date String", example = EXAMPLE_STOP_CW)
			final String stop,
			@RequestParam(required = false)
			@ApiParam(name = "targetId", value = "Target ID", example = EXAMPLE_TARGET_ID)
			final String targetId,
			@RequestParam
			@ApiParam(name = "page", value = "Page number", example = "0")
			final int page,
			@RequestParam
			@ApiParam(name = "count", value = "Number of results", example = "100")
			final int count )
			throws PlanningQueryNoParametersException {
		validateCollectionWindowParameters(	name,
											scn,
											start,
											stop);

		validatePageAndCountParameters(	page,
										count);

		final DateTime startTime = parseDateTimeString(start);
		final DateTime stopTime = parseDateTimeString(stop);

		final PageRequest pageRequest = PageRequest.of(	page,
														count,
														Sort.by("cwId"));

		final List<CollectionWindow> cws = cwRepository.findCollectionWindowsPaged(	scn,
																					name,
																					(startTime == null) ? null
																							: startTime.getMillis(),
																					(stopTime == null) ? null
																							: stopTime.getMillis(),
																					targetId,
																					pageRequest);

		return formCollectionWindowsResponse(	cws,
												start,
												stop,
												scn,
												name);
	}

	@GetMapping(value = "/collectionWindows/count")
	@ApiOperation("Gets the number of collection windows for a given list of parameters")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The number of collection windows was successfully found"),
		@ApiResponse(code = 400, message = "The input IS08601 dates could not be parsed, or not enough query parameters were specified")
	})
	public @ResponseBody ResponseEntity<Long> countCollectionWindows(
			@RequestParam(required = false)
			@ApiParam(name = "name", value = "Asset Name", example = EXAMPLE_ASSET_NAME)
			final String name,
			@RequestParam(required = false)
			@ApiParam(name = "scn", value = "Asset SCN", example = EXAMPLE_ASSET_SCN)
			final Integer scn,
			@RequestParam(required = false)
			@ApiParam(name = "start", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_CW)
			final String start,
			@RequestParam(required = false)
			@ApiParam(name = "stop", value = "ISO8601 Formatted Date String", example = EXAMPLE_STOP_CW)
			final String stop,
			@RequestParam(required = false)
			@ApiParam(name = "targetId", value = "Target ID", example = EXAMPLE_TARGET_ID)
			final String targetId )
			throws PlanningQueryNoParametersException {
		validateCollectionWindowParameters(	name,
											scn,
											start,
											stop);

		final DateTime startTime = parseDateTimeString(start);
		final DateTime stopTime = parseDateTimeString(stop);

		final long numCws = cwRepository.countCollectionWindows(scn,
																name,
																(startTime == null) ? null : startTime.getMillis(),
																(stopTime == null) ? null : stopTime.getMillis(),
																targetId);

		return ResponseEntity.ok(numCws);
	}

	/**
	 * Form a ResponseEntity from a list of collection windows.
	 *
	 * @param cws
	 *            The list of collection windows.
	 * @param start
	 *            The start time of the query interval.
	 * @param stop
	 *            The stop time of the query interval.
	 * @param scn
	 *            The spacecraft ID number for the query.
	 * @param name
	 *            The asset name for the query.
	 * @return An OK result with the list of collection windows if there were any,
	 *         or a no content result if there were no collection windows.
	 */
	private ResponseEntity<List<CollectionWindowModel>> formCollectionWindowsResponse(
			final List<CollectionWindow> cws,
			final String start,
			final String stop,
			final Integer scn,
			final String name ) {
		if ((cws == null) || cws.isEmpty()) {
			logger.warn("No collection windows found for date range: " + start + "/" + stop + " and asset: " + scn + "/"
					+ name);
			return ResponseEntity.noContent()
					.build();
		}

		final List<CollectionWindowModel> models = cws.stream()
				.map(CollectionWindow::toModel)
				.collect(Collectors.toList());

		return ResponseEntity.ok(models);
	}

	/**
	 * Ensure either the mission ID or target ID is not null.
	 *
	 * @param missionId
	 *            The mission ID.
	 * @param targetId
	 *            The target ID.
	 * @throws PlanningQueryNoParametersException
	 *             Thrown if both missionId and targetId are null.
	 */
	private static void validateTaskingParameters(
			final String missionId,
			final String targetId )
			throws PlanningQueryNoParametersException {
		// require at least missionId or targetId
		if ((missionId == null) && (targetId == null)) {
			throw new PlanningQueryNoParametersException(
					"missionId, targetId");
		}
	}

	/**
	 * Ensure the name, SCN, or start time and stop time are not null.
	 *
	 * @param name
	 *            The asset name.
	 * @param scn
	 *            The asset SCN.
	 * @param start
	 *            The start time to search for.
	 * @param stop
	 *            The stop time to search for.
	 * @throws PlanningQueryNoParametersException
	 *             Thrown if name, scn, and either start or stop are null.
	 */
	private static void validateCollectionWindowParameters(
			final String name,
			final Integer scn,
			final String start,
			final String stop )
			throws PlanningQueryNoParametersException {
		// need to at least have name, scn, or (start and stop)
		if ((name == null) && (scn == null) && ((start == null) || (stop == null))) {
			throw new PlanningQueryNoParametersException(
					"name, scn, (start and stop)");
		}
	}

	/**
	 * Parse an ISO-8601 date time string.
	 *
	 * @param dateTimeString
	 *            The string to parse, which should be in ISO-8601 format. This
	 *            parameter can be null.
	 * @return A DateTime object, or null if the string was null.
	 */
	private static DateTime parseDateTimeString(
			final String dateTimeString ) {
		if (dateTimeString == null) {
			return null;
		}

		try {
			return ISODateTimeFormat.dateTimeParser()
					.parseDateTime(dateTimeString);
		}
		catch (final Exception e) {
			logger.error("Cannot parse ISO8601 datetime String: " + dateTimeString);

			throw new BadRequestException(
					"Cannot parse ISO8601 datetime String: " + dateTimeString);
		}
	}
}
