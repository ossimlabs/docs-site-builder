package com.maxar.spaceobjectcatalog.controller;

import static com.maxar.common.utils.PaginationParameterValidator.validatePageAndCountParameters;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.common.exception.BadRequestException;
import com.maxar.spaceobjectcatalog.model.SpaceObject;
import com.maxar.spaceobjectcatalog.service.SpaceObjectCatalogService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/catalog")
public class SpaceObjectCatalogController
{
	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	@Autowired
	private SpaceObjectCatalogService spaceObjectCatalogService;

	@ApiOperation("Gets all ephemerides for an asset by SCN")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The ephemerides were successfully retrieved"),
		@ApiResponse(code = 400, message = "The SCN, page, or count parameter was invalid")
	})
	@GetMapping("/{scn}")
	public ResponseEntity<SpaceObject> getEphemerisPaginated(
			@ApiParam(name = "scn", required = true, value = "Satellite catalog number (SCN)", example = "32060")
			@PathVariable(name = "scn")
			final Integer scn,
			@ApiParam(name = "page", required = true, value = "Page number (zero indexed)", example = "0")
			@RequestParam(name = "page")
			final Integer page,
			@ApiParam(name = "count", required = true, value = "Number of ephemerides per page", example = "100")
			@RequestParam(name = "count")
			final Integer count ) {
		validateScn(scn);
		validatePageAndCountParameters(	page,
										count);

		final SpaceObject spaceObject = spaceObjectCatalogService.getEphemerisPaginated(scn,
																						page,
																						count);

		return ResponseEntity.ok(spaceObject);
	}

	@ApiOperation("Gets the number of ephemerides for an asset by SCN")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The ephemerides were successfully counted"),
		@ApiResponse(code = 400, message = "The SCN parameter was invalid")
	})
	@GetMapping("/{scn}/count")
	public ResponseEntity<Long> getEphemerisCount(
			@ApiParam(name = "scn", required = true, value = "Satellite catalog number (SCN)", example = "32060")
			@PathVariable(name = "scn")
			final Integer scn ) {
		validateScn(scn);

		final long count = spaceObjectCatalogService.getEphemerisCount(scn);

		return ResponseEntity.ok(count);
	}

	@ApiOperation("Gets ephemerides for an asset by SCN within a date range")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The ephemerides were successfully retrieved"),
		@ApiResponse(code = 400, message = "The SCN, start, or end parameter was invalid")
	})
	@GetMapping("/{scn}/range")
	public ResponseEntity<SpaceObject> getEphemerisInDateRange(
			@ApiParam(name = "scn", required = true, value = "Satellite catalog number (SCN)", example = "32060")
			@PathVariable(name = "scn")
			final Integer scn,
			@ApiParam(name = "start", required = true, value = "Start date of query range (ISO-8601)", example = "2019-12-31T00:00:00Z")
			@RequestParam(name = "start")
			final String start,
			@ApiParam(name = "end", required = true, value = "End date of query range (ISO-8601)", example = "2020-01-01T00:00:00Z")
			@RequestParam(name = "end")
			final String end ) {
		validateScn(scn);

		final DateTime startTime = parseIso8601DateString(start);
		final DateTime endTime = parseIso8601DateString(end);

		final SpaceObject spaceObject = spaceObjectCatalogService.getEphemerisInDateRange(	scn,
																							startTime,
																							endTime);

		return ResponseEntity.ok(spaceObject);
	}

	/**
	 * Ensure a requested satellite catalog number (SCN) is valid.
	 *
	 * A BadRequestException will be thrown if the SCN is null or if it is not at
	 * least zero or not greater than 99999.
	 *
	 * @param scn
	 *            The satellite catalog number (SCN) to validate.
	 */
	private void validateScn(
			final Integer scn ) {
		if (scn == null) {
			throw new BadRequestException(
					"Satellite catalog number must be non-null");
		}
		if (scn < 0 || scn > 99999) {
			throw new BadRequestException(
					"Satellite catalog number must be in range [0, 99999]");
		}
	}

	/**
	 * Parse an ISO-8601 date and time string.
	 *
	 * A BadRequestException will be thrown if the string is null or if it cannot be
	 * parsed as a DateTime object.
	 *
	 * @param date
	 *            The string to parse, which should be in ISO-8601 format.
	 * @return The parsed DateTime object.
	 */
	private DateTime parseIso8601DateString(
			final String date ) {
		if (date != null && !date.isEmpty()) {
			try {
				return DateTime.parse(date);
			}
			catch (final Exception ignored) {}
		}

		throw new BadRequestException(
				"Unable to parse ISO-8601 string, " + date);
	}
}
