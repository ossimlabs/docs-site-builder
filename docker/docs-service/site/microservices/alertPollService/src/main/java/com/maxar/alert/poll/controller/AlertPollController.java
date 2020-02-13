package com.maxar.alert.poll.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.alert.poll.service.AlertPollService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/poll")
public class AlertPollController
{
	@Autowired
	private AlertPollService alertPollService;

	@ApiOperation("Pauses the polling service")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The request was okay")
	})
	@GetMapping("/pause")
	public ResponseEntity<String> pause() {
		final boolean wasNotPaused = alertPollService.pause();

		if (wasNotPaused) {
			return ResponseEntity.ok("paused");
		}
		else {
			return ResponseEntity.ok("already paused");
		}
	}

	@ApiOperation("Resumes the polling service")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The request was okay")
	})
	@GetMapping("/resume")
	public ResponseEntity<String> resume() {
		final boolean wasPaused = alertPollService.resume();

		if (wasPaused) {
			return ResponseEntity.ok("resumed");
		}
		else {
			return ResponseEntity.ok("was not paused");
		}
	}

	@ApiOperation("Issues a poll action")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The request was successful"),
		@ApiResponse(code = 400, message = "An invalid option was requested")
	})
	@GetMapping("/poll")
	public ResponseEntity<String> pollForAlerts(
			@RequestParam(name = "force", required = false, defaultValue = "true")
			final boolean forcePoll ) {
		if (alertPollService.pollForAlerts(forcePoll)) {
			return ResponseEntity.ok("polled");
		}
		else {
			return ResponseEntity.ok("did not poll");
		}
	}
}
