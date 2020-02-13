package com.maxar.terrain.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.Interval;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.common.exception.BadRequestException;
import com.maxar.terrain.service.TerrainService;
import com.maxar.terrain.model.GoodTimeIntervalsRequest;
import com.maxar.terrain.model.GoodTimeIntervalsResponse;
import com.maxar.terrain.model.TimeInterval;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api
public class TerrainController
{
	@Autowired
	private TerrainService terrainService;

	@CrossOrigin
	@PostMapping(value = "/")
	@ApiOperation("Gets the good time intervals based on a generated terrain mask")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The intervals were successfully computed"),
		@ApiResponse(code = 400, message = "The input geometry well-known text (WKT) could not be parsed")
	})
	public ResponseEntity<GoodTimeIntervalsResponse> getGoodTimeIntervals(
			@ApiParam(value = "The input parameters to calculate good intervals for")
			@RequestBody
			final GoodTimeIntervalsRequest requestBody ) {
		try {
			WKTReader reader = new WKTReader();
			final Geometry geometry = reader.read(requestBody.getGeometryWkt());

			final List<Interval> goodIntervals = terrainService.getGoodTimeIntervals(	geometry,
																						requestBody.getStateVectors());

			final List<TimeInterval> goodTimeIntervals = goodIntervals.stream()
					.map(interval -> {
						TimeInterval timeInterval = new TimeInterval();
						timeInterval.setStart(interval.getStart());
						timeInterval.setEnd(interval.getEnd());

						return timeInterval;
					})
					.collect(Collectors.toList());

			GoodTimeIntervalsResponse response = new GoodTimeIntervalsResponse();
			response.setTimeIntervals(goodTimeIntervals);

			return ResponseEntity.ok(response);
		}
		catch (final ParseException e) {
			throw new BadRequestException(
					"Unable to parse geometry WKT");
		}
	}
}
