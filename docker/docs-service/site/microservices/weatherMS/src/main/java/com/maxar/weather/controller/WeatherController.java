package com.maxar.weather.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.common.exception.BadRequestException;
import com.maxar.weather.model.map.RTNEPHModel;
import com.maxar.weather.model.map.WTMModel;
import com.maxar.weather.model.weather.CloudCoverAtTime;
import com.maxar.weather.model.weather.WeatherByDate;
import com.maxar.weather.model.weather.WeatherByDateAndGeometryRequest;
import com.maxar.weather.model.weather.WeatherByDateAndGeometryRequestBody;
import com.maxar.weather.model.weather.WeatherByGeometry;
import com.maxar.weather.entity.map.RTNEPHQuarterGrid;
import com.maxar.weather.entity.map.WTM;
import com.maxar.weather.entity.weather.Weather;
import com.maxar.weather.entity.weather.WeatherSet;
import com.maxar.weather.repository.RTNEPHQuarterGridRepository;
import com.maxar.weather.repository.WTMRepository;
import com.maxar.weather.repository.WeatherRepository;
import com.maxar.weather.repository.WeatherSetRepository;
import com.radiantblue.analytics.core.log.SourceLogger;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "/weatherMS")
public class WeatherController
{
	private static Logger logger = SourceLogger.getLogger(WeatherController.class.getName());

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	// Static strings for swagger examples
	private static final String EXAMPLE_WKT_STRING = "POLYGON((120.0 31.0,120.6 31.0,"
			+ "120.6 30.8,120.0 30.8,120.0 31.0))";
	private static final String EXAMPLE_START_DATE = "2020-01-01T06:00:00Z";
	private static final String EXAMPLE_END_DATE = "2020-01-01T09:00:00Z";

	@Autowired
	private WTMRepository wtmRepository;

	@Autowired
	private RTNEPHQuarterGridRepository rtnephRepository;

	@Autowired
	private WeatherSetRepository weatherSetRepository;

	@Autowired
	private WeatherRepository weatherRepository;

	@GetMapping(value = "/weather")
	@ApiOperation("Gets the cloud cover for a geometry for a given time")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The cloud cover was successfully found"),
		@ApiResponse(code = 400, message = "The input geometry well-known text (WKT) or ISO8601 date could not be parsed")
	})
	public @ResponseBody ResponseEntity<Double> getWeatherByWKTGeometryStringAndDate(
			@RequestParam
			@ApiParam(name = "geometry", value = "Geometry String using well known text (WKT)", example = EXAMPLE_WKT_STRING)
			final String geometry,
			@RequestParam
			@ApiParam(name = "date", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_DATE)
			final String date ) {
		final Geometry geom = parseWkt(geometry);
		final DateTime atTime = parseDateTimeString(date);

		// there is no max in jpa query apparently so need to do this in two
		// steps
		final List<WeatherSet> weatherSets = weatherSetRepository
				.findByAtTimeMillisLessThanEqualOrderByAtTimeMillisDesc(atTime.getMillis());

		if ((weatherSets == null) || weatherSets.isEmpty()) {
			logger.warn("No weather sets found for date: " + date);
			return ResponseEntity.ok(0.0);
		}

		final List<Weather> weathers = weatherRepository.getByWeatherSetAndGeometry(weatherSets.get(0)
				.getWeatherSetKey(),
																					geom);
		// use simple average for now
		final Double cloudCoverPercent = weathers.stream()
				.mapToDouble(Weather::getCloudCoverPercent)
				.average()
				.orElse(0.0);

		return ResponseEntity.ok(cloudCoverPercent);
	}

	@GetMapping(value = "/weathers")
	@ApiOperation("Gets the cloud cover values for a geometry for a given time range")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The cloud cover was successfully found"),
		@ApiResponse(code = 400, message = "The input geometry well-known text (WKT) or ISO8601 date could not be parsed")
	})
	public @ResponseBody ResponseEntity<List<CloudCoverAtTime>> getWeatherByWKTGeometryStringAndDateRange(
			@RequestParam
			@ApiParam(name = "geometry", value = "Geometry String using well known text (WKT)", example = EXAMPLE_WKT_STRING)
			final String geometry,
			@RequestParam
			@ApiParam(name = "start", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_DATE)
			final String start,
			@RequestParam
			@ApiParam(name = "stop", value = "ISO8601 Formatted Date String", example = EXAMPLE_END_DATE)
			final String stop ) {
		final Geometry geom = parseWkt(geometry);
		final DateTime startTime = parseDateTimeString(start);
		final DateTime endTime = parseDateTimeString(stop);

		final List<WeatherSet> weatherSets = weatherSetRepository.findByAtTimeMillisBetween(startTime.getMillis(),
																							endTime.getMillis());

		if ((weatherSets == null) || weatherSets.isEmpty()) {
			logger.warn("No weather sets found for date range: " + start + "/" + stop);
			return ResponseEntity.ok()
					.build();
		}

		final List<CloudCoverAtTime> cloudCoverList = new ArrayList<>();

		for (final WeatherSet ws : weatherSets) {
			final List<Weather> weathers = weatherRepository.getByWeatherSetAndGeometry(ws.getWeatherSetKey(),
																						geom);
			// use simple average for now
			final Double cloudCoverPercent = weathers.stream()
					.mapToDouble(Weather::getCloudCoverPercent)
					.average()
					.orElse(0.0);

			final String isoAtTime = ISODateTimeFormat.dateTime()
					.print(ws.getAtTimeMillis());
			final CloudCoverAtTime ccAtTime = new CloudCoverAtTime(
					isoAtTime,
					cloudCoverPercent);

			cloudCoverList.add(ccAtTime);
		}

		return ResponseEntity.ok(cloudCoverList);
	}

	@GetMapping(value = "/weatherByGeometry")
	@ApiOperation("Gets the cloud cover values and geometries for a given time range")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The cloud cover was successfully found"),
		@ApiResponse(code = 400, message = "The input geometry well-known text (WKT) or ISO8601 date could not be parsed")
	})
	@Transactional
	public @ResponseBody ResponseEntity<List<WeatherByDate>> getWeatherAndGeometryByWKTGeometryStringAndDateRange(
			@RequestParam
			@ApiParam(name = "geometry", value = "Geometry String using well known text (WKT)", example = EXAMPLE_WKT_STRING)
			final String geometry,
			@RequestParam
			@ApiParam(name = "start", value = "ISO8601 Formatted Date String", example = EXAMPLE_START_DATE)
			final String start,
			@RequestParam
			@ApiParam(name = "stop", value = "ISO8601 Formatted Date String", example = EXAMPLE_END_DATE)
			final String stop ) {
		final Geometry geom = parseWkt(geometry);
		final DateTime startTime = parseDateTimeString(start);
		final DateTime endTime = parseDateTimeString(stop);

		final List<WeatherSet> weatherSets = weatherSetRepository.findByAtTimeMillisBetween(startTime.getMillis(),
																							endTime.getMillis());

		if ((weatherSets == null) || weatherSets.isEmpty()) {
			logger.warn("No weather sets found for date range: " + start + "/" + stop);
			return ResponseEntity.ok()
					.build();
		}

		final List<WeatherByDate> returnList = new ArrayList<>();

		for (final WeatherSet ws : weatherSets) {
			final List<Weather> weathers = weatherRepository.getByWeatherSetAndGeometry(ws.getWeatherSetKey(),
																						geom);

			final List<WeatherByGeometry> weathersByGeometry = weathers.stream()
					.map(w -> new WeatherByGeometry(
							w.getCloudCoverPercent() / 100.0,
							w.getMapGrid()
									.getGeometry()))
					.collect(Collectors.toList());

			final String isoAtTime = ISODateTimeFormat.dateTime()
					.print(ws.getAtTimeMillis());
			final WeatherByDate ccAtTime = new WeatherByDate(
					isoAtTime,
					weathersByGeometry,
					null);

			returnList.add(ccAtTime);
		}

		return ResponseEntity.ok(returnList);
	}

	@PostMapping(value = "/weathersByGeometriesAndTimes")
	@ApiOperation("Gets the cloud cover values and geometries for given geometries and times")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The request was successful"),
		@ApiResponse(code = 400, message = "The input geometry well-known text (WKT) or ISO8601 date could not be parsed")
	})
	@Transactional
	public @ResponseBody ResponseEntity<List<WeatherByDate>> getWeathersAndGeometriesByWKTGeometryStringsAndDates(
			@ApiParam(value = "The input parameters to find weather for")
			@RequestBody
			final WeatherByDateAndGeometryRequestBody requestBody ) {
		if (requestBody == null || requestBody.getWeatherRequestList() == null) {
			final String errorString = "Weather request is null";
			logger.error(errorString);
			throw new BadRequestException(
					errorString);
		}

		final List<WeatherByDate> returnList = requestBody.getWeatherRequestList()
				.stream()
				.map(weatherRequest -> processWeatherRequest(	weatherRequest,
																requestBody.getParentId()))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		return ResponseEntity.ok(returnList);
	}

	private WeatherByDate processWeatherRequest(
			final WeatherByDateAndGeometryRequest req,
			final String parentId ) {
		final Geometry geom = parseWkt(req.getGeometryWKT());
		final DateTime atTime = parseDateTimeString(req.getDateTimeISO8601());

		// there is no max in jpa query apparently so need to do this in two
		// steps
		final List<WeatherSet> weatherSets = weatherSetRepository
				.findByAtTimeMillisLessThanEqualOrderByAtTimeMillisDesc(atTime.getMillis());

		if ((weatherSets == null) || weatherSets.isEmpty()) {
			logger.warn("No weather sets found for date: " + req.getDateTimeISO8601());
			return null;
		}

		final List<Weather> weathers = weatherRepository.getByWeatherSetAndGeometry(weatherSets.get(0)
				.getWeatherSetKey(),
																					geom);

		if ((weathers == null) || weathers.isEmpty()) {
			return null;
		}

		final List<WeatherByGeometry> weathersByGeometry = weathers.stream()
				.map(w -> new WeatherByGeometry(
						w.getCloudCoverPercent() / 100.0,
						w.getMapGrid()
								.getGeometry()))
				.collect(Collectors.toList());

		return new WeatherByDate(
				req.getDateTimeISO8601(),
				weathersByGeometry,
				parentId);
	}

	@GetMapping(value = "/wtms")
	@ApiOperation("Gets the WTMs for a geometry")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The WTMs were successfully found"),
		@ApiResponse(code = 204, message = "No WTMs were found for given geometry"),
		@ApiResponse(code = 400, message = "The input geometry well-known text (WKT) could not be parsed")
	})
	public @ResponseBody ResponseEntity<List<WTMModel>> getWTMsByWKTGeometryString(
			@RequestParam
			@ApiParam(name = "geometry", value = "Geometry String using well known text (WKT)", example = EXAMPLE_WKT_STRING)
			final String geometry ) {
		final Geometry geom = parseWkt(geometry);

		final List<WTM> wtms = wtmRepository.findByGeometry(geom);

		if (wtms.isEmpty()) {
			return ResponseEntity.noContent()
					.build();
		}
		final List<WTMModel> wtmModels = wtms.stream()
				.map(WTM::toModel)
				.collect(Collectors.toList());

		return ResponseEntity.ok(wtmModels);
	}

	@GetMapping(value = "/rtnephs")
	@ApiOperation("Gets the RTNEPHQuarterGrids for a geometry")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The RTNEPHQuarterGrids was successfully found"),
		@ApiResponse(code = 204, message = "No RTNEPHQuarterGrids were found for given geometry"),
		@ApiResponse(code = 400, message = "The input geometry well-known text (WKT) could not be parsed")
	})
	public @ResponseBody ResponseEntity<List<RTNEPHModel>> getRTNPHSsByWKTGeometryString(
			@RequestParam
			@ApiParam(name = "geometry", value = "Geometry String using well known text (WKT)", example = EXAMPLE_WKT_STRING)
			final String geometry ) {
		final Geometry geom = parseWkt(geometry);

		final List<RTNEPHQuarterGrid> rtnephs = rtnephRepository.findByGeometry(geom);

		if (rtnephs.isEmpty()) {
			return ResponseEntity.noContent()
					.build();
		}

		final List<RTNEPHModel> rtnephModels = rtnephs.stream()
				.map(RTNEPHQuarterGrid::toModel)
				.collect(Collectors.toList());

		return ResponseEntity.ok(rtnephModels);
	}

	@GetMapping(value = "/wtms/id/{id}")
	@ApiOperation("Gets the WTM for a given id")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The WTM was successfully found"),
		@ApiResponse(code = 400, message = "The input geometry well-known text (WKT) could not be parsed"),
		@ApiResponse(code = 404, message = "No WTMs were found with given id")
	})
	public @ResponseBody ResponseEntity<List<WTMModel>> getWTMsByMapGridId(
			@PathVariable
			@ApiParam(name = "id", value = "8 digit wtm ID", example = "01200101")
			final String id ) {
		final List<WTM> wtms = wtmRepository.findById(id);

		if (wtms.isEmpty()) {
			return ResponseEntity.notFound()
					.build();
		}

		final List<WTMModel> wtmModels = wtms.stream()
				.map(WTM::toModel)
				.collect(Collectors.toList());

		return ResponseEntity.ok(wtmModels);
	}

	@GetMapping(value = "/rtneph/id/{id}")
	@ApiOperation("Gets the RTNEPHQuarterGrid for a given id")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The RTNEPHQuarterGrid was successfully found"),
		@ApiResponse(code = 400, message = "The input geometry well-known text (WKT) could not be parsed"),
		@ApiResponse(code = 404, message = "No RTNEPHQuarterGrids were found with given id")
	})
	public @ResponseBody ResponseEntity<List<RTNEPHModel>> getRTNEPHsByMapGridId(
			@PathVariable
			@ApiParam(name = "id", value = "5 digit rtneph ID", example = "47610")
			final String id ) {
		final List<RTNEPHQuarterGrid> rtnephs = rtnephRepository.findById(id);

		if (rtnephs.isEmpty()) {
			return ResponseEntity.notFound()
					.build();
		}

		final List<RTNEPHModel> rtnephModels = rtnephs.stream()
				.map(RTNEPHQuarterGrid::toModel)
				.collect(Collectors.toList());

		return ResponseEntity.ok(rtnephModels);
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
	 * Parse a well-known text (WKT) geometry, or throw a BadRequestException if the
	 * WKT cannot be parsed.
	 *
	 * @param geometry
	 *            The geometry to parse in WKT format.
	 * @return A geometry object parsed from the input string.
	 */
	private static Geometry parseWkt(
			final String geometry ) {
		final String errorString = "Cannot parse WKT string: " + geometry;

		if (geometry == null || geometry.isEmpty()) {
			logger.error(errorString);
			throw new BadRequestException(
					errorString);
		}

		final WKTReader reader = new WKTReader();

		try {
			return reader.read(geometry);
		}
		catch (final ParseException e) {
			logger.error(errorString);
			throw new BadRequestException(
					errorString);
		}
	}
}
