package com.maxar.workflow.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.target.model.TargetModel;
import com.maxar.workflow.service.WorkflowTargetService;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/target")
public class WorkflowTargetController
{
	@Autowired
	private WorkflowTargetService workflowTargetService;

	// Static strings for swagger examples
	private static final String EXAMPLE_WKT_STRING = "POLYGON((24.63719 29.79381, 29.79381 29.79381, 29.79381 24.63719,"
			+ "24.63719 24.63719, 24.63719 29.79381))";

	@CrossOrigin
	@GetMapping("/targetsbygeometry")
	@ApiOperation("Gets the Targets for a given geometry")
	@ApiResponses({
						  @ApiResponse(code = 200, message = "The Targets were successfully found"),
						  @ApiResponse(code = 204, message = "No Targets were found for given geometry"),
						  @ApiResponse(code = 400, message = "The input geometry well-known text (WKT) could not be parsed")
				  })
	public @ResponseBody
	ResponseEntity<List<TargetModel>> getTargetsByGeometry(
			@RequestParam
			@ApiParam(name = "geometry", value = "Geometry String using well known text (WKT)", example = EXAMPLE_WKT_STRING)
			final String geometry) {


		final List<TargetModel> targetsList = workflowTargetService.getTargetsByGeometry(geometry);

		return ResponseEntity.ok(targetsList);


	}


}