package com.maxar.ephemeris.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.common.exception.BadRequestException;
import com.maxar.common.types.VehiclePosition;
import com.maxar.ephemeris.entity.Ephemeris;
import com.maxar.ephemeris.entity.StateVectorSet;
import com.maxar.ephemeris.entity.TLE;
import com.maxar.ephemeris.entity.VCM;
import com.maxar.ephemeris.model.EphemerisModel;
import com.maxar.ephemeris.model.TLEModel;
import com.maxar.ephemeris.repository.StateVectorSetRepository;
import com.maxar.ephemeris.repository.TLERepository;
import com.maxar.ephemeris.repository.VCMRepository;
import com.maxar.ephemeris.utils.EphemerisUtils;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "/ephemerisMS")
public class EphemerisController
{
	private static Logger logger = SourceLogger.getLogger(EphemerisController.class.getName());

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	// Static strings for swagger examples
	private static final String EXAMPLE_SCN = "32382";
	private static final String EXAMPLE_DATE = "2020-01-01T12:00:00Z";
	private static final String EXAMPLE_END_DATE = "2020-01-01T13:00:00Z";

	@Value("${priority.ephemeris.vcm}")
	private Integer vcmPriority;

	@Value("${priority.ephemeris.stateVector}")
	private Integer stateVectorPriority;

	@Value("${priority.ephemeris.tle}")
	private Integer tlePriority;

	@Value("${priority.ephemeris.timingThresholdMillis}")
	private Long timingThresholdMillis;

	@Autowired
	private TLERepository tleRepository;

	@Autowired
	private StateVectorSetRepository stateVectorSetRepository;

	@Autowired
	private VCMRepository vcmRepository;

	@GetMapping("/ephemeris")
	@ApiOperation("Gets an ephemeris for a given SCN with optional date. Type is TLE, State Vectors, or VCM depending on priority and age relative to each type.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The ephemeris was successfully found"),
		@ApiResponse(code = 204, message = "No ephemeris found for given SCN"),
		@ApiResponse(code = 400, message = "SCN or iso8601DateString could not be parsed")
	})
	public @ResponseBody ResponseEntity<EphemerisModel> getEphemeris(
			@RequestParam
			@ApiParam(name = "scn", value = "SCN", example = EXAMPLE_SCN)
			final String scn,
			@RequestParam(required = false)
			@ApiParam(name = "date", value = "ISO8601 Formatted Date String", example = EXAMPLE_DATE)
			final String date ) {
		final int scnAsInteger = parseInteger(scn);

		final TLE tle;
		final StateVectorSet svs;
		final VCM vcm;

		if (date == null) {
			tle = tleRepository.findFirstByScnOrderByEpochMillisDesc(scnAsInteger);
			svs = stateVectorSetRepository.findFirstByScnOrderByEpochMillisDesc(scnAsInteger);
			vcm = vcmRepository.findFirstByScnOrderByEpochMillisDesc(scnAsInteger);
		}
		else {
			final DateTime atTime = parseDateTimeString(date);

			tle = tleRepository.findFirstByScnAndEpochMillisLessThanEqualOrderByEpochMillisDesc(scnAsInteger,
																								atTime.getMillis());
			svs = stateVectorSetRepository.findFirstByScnAndEpochMillisLessThanEqualOrderByEpochMillisDesc(	scnAsInteger,
																											atTime.getMillis());
			vcm = vcmRepository.findFirstByScnAndEpochMillisLessThanEqualOrderByEpochMillisDesc(scnAsInteger,
																								atTime.getMillis());
		}

		if ((tle == null) && (svs == null) && (vcm == null)) {
			return ResponseEntity.noContent()
					.build();
		}

		final List<Ephemeris> ephemerisList = new ArrayList<>();

		if (tle != null) {
			tle.setPriority(tlePriority);
			ephemerisList.add(tle);
		}

		if (svs != null) {
			svs.setPriority(stateVectorPriority);
			ephemerisList.add(svs);
		}

		if (vcm != null) {
			vcm.setPriority(vcmPriority);
			ephemerisList.add(vcm);
		}

		if (ephemerisList.size() == 1) {
			return ResponseEntity.ok(ephemerisList.get(0)
					.toModel());
		}

		final Ephemeris ephemerisToReturn = EphemerisUtils
				.calculateEphemerisTypeByPriorityAndTimingThreshold(ephemerisList,
																	timingThresholdMillis);

		return ResponseEntity.ok(ephemerisToReturn.toModel());
	}

	@GetMapping("/vehiclePositions")
	public @ResponseBody ResponseEntity<List<VehiclePosition>> getPositions(
			@RequestParam
			@ApiParam(name = "scn", value = "SCN", example = EXAMPLE_SCN)
			final String scn,
			@RequestParam
			@ApiParam(name = "startDateString", value = "ISO8601 Formatted Start Date String", example = EXAMPLE_DATE)
			final String startDateString,
			@RequestParam
			@ApiParam(name = "endDateString", value = "ISO8601 Formatted End Date String", example = EXAMPLE_END_DATE)
			final String endDateString,
			@RequestParam
			@ApiParam(name = "samplingInterval_ms", value = "Sampling Interval for position Data (ms)", example = "1000")
			final String samplingInterval_ms ) {
		final int scnAsInteger = parseInteger(scn);
		final DateTime start = parseDateTimeString(startDateString);
		final DateTime end = parseDateTimeString(endDateString);
		final int samplingIntervalAsInteger = parseInteger(samplingInterval_ms);

		final TLE tle = tleRepository.findFirstByScnAndEpochMillisLessThanEqualOrderByEpochMillisDesc(	scnAsInteger,
																										start.getMillis());

		final List<StateVectorsInFrame> svifList = EphemerisUtils.tleToStateVectors(start,
																					end,
																					samplingIntervalAsInteger,
																					(TLEModel) tle.toModel());

		final List<VehiclePosition> positions = new ArrayList<>();

		for (final StateVectorsInFrame svif : svifList) {
			final VehiclePosition vp = new VehiclePosition();
			vp.setId(scn);
			vp.setSvif(svif);

			positions.add(vp);
		}

		return ResponseEntity.ok(positions);
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
}
