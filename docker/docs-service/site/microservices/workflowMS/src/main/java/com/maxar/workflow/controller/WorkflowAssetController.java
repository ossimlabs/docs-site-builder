package com.maxar.workflow.controller;

import java.util.List;
import java.util.SortedMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.asset.model.NameList;
import com.maxar.workflow.service.WorkflowAssetService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("asset")
public class WorkflowAssetController
{
	@Autowired
	private WorkflowAssetService workflowAssetService;

	@CrossOrigin
	@GetMapping(value = "/space/name")
	@ApiOperation("Gets the names of all space assets")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The names of space assets were successfully retrieved")
	})
	public ResponseEntity<NameList> getSpaceNames() {
		final NameList names = workflowAssetService.getSpaceAssetNames();

		return ResponseEntity.ok(names);
	}

	@CrossOrigin
	@GetMapping(value = "/space/sensorType/withNames")
	@ApiOperation("Gets all the space assets with sensor types and assets names")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The space assets were successfully retrieved")
	})
	public ResponseEntity<SortedMap<String, List<String>>> getSpaceSensorTypesNames(
			@RequestParam(required = false)
			final List<String> sensorTypesNeeded ) {
		final SortedMap<String, List<String>> assets = workflowAssetService
				.getSpaceSensorTypesWithNames(sensorTypesNeeded);

		return ResponseEntity.ok(assets);
	}

	@CrossOrigin
	@GetMapping(value = "/space/sensorType/withIds")
	@ApiOperation("Gets all the space assets with sensor types and assets IDs")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The space assets were successfully retrieved")
	})
	public ResponseEntity<SortedMap<String, List<String>>> getSpaceSensorTypesIds(
			@RequestParam(required = false)
			final List<String> sensorTypesNeeded ) {
		final SortedMap<String, List<String>> assets = workflowAssetService
				.getSpaceSensorTypesWithIds(sensorTypesNeeded);

		return ResponseEntity.ok(assets);
	}
}
