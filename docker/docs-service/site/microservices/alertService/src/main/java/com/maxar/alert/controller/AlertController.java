package com.maxar.alert.controller;

import java.net.URI;
import java.util.List;

import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.alert.model.Event;
import com.maxar.alert.exception.AlertIdDoesNotExistException;
import com.maxar.alert.exception.AlertIdExistsException;
import com.maxar.alert.exception.InvalidAlertException;
import com.maxar.alert.exception.InvalidRequestException;
import com.maxar.alert.service.AlertService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/alert")
public class AlertController
{
	@Autowired
	private AlertService alertService;

	@ApiOperation("Creates a new alert")
	@ApiResponses({
		@ApiResponse(code = 201, message = "The new alert was successfully added"),
		@ApiResponse(code = 400, message = "There was a problem with the request"),
		@ApiResponse(code = 409, message = "There is an existing alert with the same ID as the requested alert")
	})
	@PostMapping(value = "")
	public ResponseEntity<String> createAlert(
			@RequestBody
			final Event event )
			throws AlertIdExistsException,
			InvalidAlertException {
		try {
			final String id = alertService.createAlert(event);
			final URI uri = URI.create("/alert/" + id);

			return ResponseEntity.created(uri)
					.build();
		}
		catch (final RuntimeException e) {
			if (e.getCause() != null && e.getCause()
					.getClass()
					.equals(ParseException.class)) {
				return ResponseEntity.badRequest()
						.body(e.getCause()
								.getMessage());
			}
			else {
				throw e;
			}
		}
	}

	@ApiOperation("Gets the IDs of all existing alerts")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The alert IDs were successfully retrieved")
	})
	@GetMapping(value = "")
	public ResponseEntity<List<String>> getAlerts() {
		final List<String> ids = alertService.getAllAlerts();

		return ResponseEntity.ok(ids);
	}

	@ApiOperation("Gets an existing alert by ID")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The alert was successfully retrieved"),
		@ApiResponse(code = 404, message = "There is no alert with the requested ID")
	})
	@GetMapping(value = "/{id}")
	public ResponseEntity<Event> getAlertById(
			@PathVariable(name = "id")
			final String id )
			throws AlertIdDoesNotExistException {
		final Event event = alertService.getAlertById(id);

		return ResponseEntity.ok(event);
	}

	@GetMapping(value = "/geometry")
	@ApiOperation("Gets events with geometries intersecting a specified geometry")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The events were successfully retrieved"),
		@ApiResponse(code = 400, message = "The requested geometry was not valid WKT")
	})
	public ResponseEntity<List<Event>> getAlertByGeometry(
			@RequestParam
			@ApiParam(name = "geometry", value = "String", example = "POLYGON ((30.509033203124996 "
					+ "25.962983554822678, 30.487060546875 25.839449402063185, 30.761718749999996 "
					+ "25.78505344378837, 30.827636718749996 25.898761936567023, 30.509033203124996 25.962983554822678))")
			final String geometry )
			throws InvalidRequestException {
		final List<Event> events = alertService.getAlertsByGeometry(geometry);

		return ResponseEntity.ok(events);
	}

	@ApiOperation("Deletes an existing alert")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The alert was successfully deleted"),
		@ApiResponse(code = 404, message = "There is no alert with the requested ID")
	})
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<String> deleteAlertById(
			@PathVariable(name = "id")
			final String id )
			throws AlertIdDoesNotExistException {
		alertService.deleteAlertById(id);

		return ResponseEntity.ok()
				.build();
	}
}
