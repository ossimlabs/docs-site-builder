package com.maxar.workflow.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxar.workflow.model.CzmlErrorReporterWithId;
import com.maxar.workflow.service.WorkflowMapService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/map")
public class WorkflowMapController
{
	@Autowired
	private WorkflowMapService workflowMapService;

	@CrossOrigin
	@DeleteMapping(value = "/delete/czml")
	@ApiOperation("Generates delete request to delete all czml in provided session")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The CZML delete request was successfully generated and sent to the Cesium server"),
		@ApiResponse(code = 400, message = "There was a problem processing the delete request"),
		@ApiResponse(code = 404, message = "There was a problem processing the delete request")
	})
	public ResponseEntity<CzmlErrorReporterWithId> deleteCzml(
			@RequestParam(required = false, defaultValue = "")
			final String session ) {
		final CzmlErrorReporterWithId czmlErrorReporterWithId = workflowMapService.deleteAllCzml(session);

		if (czmlErrorReporterWithId == null) {
			return ResponseEntity.ok()
					.build();
		}
		else {
			return ResponseEntity.badRequest()
					.body(czmlErrorReporterWithId);
		}
	}

	@CrossOrigin
	@PostMapping(value = "/display/czml")
	@ApiOperation("Displays provided CZML packets for provided session")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The provided CZML packets were successfully sent to the Cesium server"),
		@ApiResponse(code = 400, message = "There was a problem processing the request"),
		@ApiResponse(code = 404, message = "There was a problem processing the request")
	})
	public ResponseEntity<CzmlErrorReporterWithId> displayCzml(
			@RequestBody
			final List<JsonNode> czml,
			@RequestParam(defaultValue = "")
			final String session ) {
		final CzmlErrorReporterWithId czmlErrorReporterWithId = workflowMapService.displayCzml(	session,
																								czml);

		if (czmlErrorReporterWithId == null) {
			return ResponseEntity.ok()
					.build();
		}
		else {
			return ResponseEntity.badRequest()
					.body(czmlErrorReporterWithId);
		}
	}
}
