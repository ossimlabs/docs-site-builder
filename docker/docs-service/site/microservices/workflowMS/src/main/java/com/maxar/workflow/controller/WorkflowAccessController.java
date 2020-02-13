package com.maxar.workflow.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.access.model.AccessValues;
import com.maxar.workflow.model.Access;
import com.maxar.workflow.model.AccessDetailsRequest;
import com.maxar.workflow.model.TargetAccessRequest;
import com.maxar.workflow.model.TargetGeometryAccessRequest;
import com.maxar.workflow.service.WorkflowAccessService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/access")
public class WorkflowAccessController
{
	@Autowired
	private WorkflowAccessService workflowAccessService;

	@CrossOrigin
	@PostMapping(value = "/target/access")
	@ApiOperation("Gets the accesses for the targets during the specified event time interval")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Access generation succeeded for the specified target"),
		@ApiResponse(code = 400, message = "There was a problem with the access request")
	})
	public ResponseEntity<List<Access>> getAccessesForTarget(
			@RequestBody
			final TargetAccessRequest accessRequest ) {
		final List<Access> accesses = workflowAccessService.getAccessesForTargetsAndAssets(accessRequest);

		return ResponseEntity.ok(accesses);
	}

	@CrossOrigin
	@PostMapping(value = "/target/geometry")
	@ApiOperation("Gets the accesses for the target geometries during the specified event time interval")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Access generation succeeded for the specified target geometry"),
		@ApiResponse(code = 400, message = "There was a problem with the access request")
	})
	public ResponseEntity<List<Access>> getAccessesForTargetGeometry(
			@RequestBody
			final TargetGeometryAccessRequest accessRequest ) {
		final List<Access> accesses = workflowAccessService.getAccessesForTargetGeometriesAndAssets(accessRequest);
		for (int i = 0; i < accesses.size(); ++i) {
			accesses.get(i).setIndex(i);
		}
		return ResponseEntity.ok(accesses);
	}

	@CrossOrigin
	@GetMapping(value = "/constraints")
	@ApiOperation("Gets list of available access constraints")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The constraints were successfully retrieved"),
		@ApiResponse(code = 404, message = "Unable to retrieve constraints")
	})
	public ResponseEntity<List<String>> getAccessConstraintNames() {
		final List<String> constraints = workflowAccessService.getAccessConstraintNames();
		return ResponseEntity.ok(constraints);
	}

	@CrossOrigin
	@PostMapping(value = "/details")
	@ApiOperation("Gets the accesses for the target geometries during the specified event time interval")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Access generation succeeded for the specified target geometry"),
		@ApiResponse(code = 400, message = "There was a problem with the access request")
	})
	public ResponseEntity<AccessValues> getAccessDetails(
			@RequestBody
			final AccessDetailsRequest accessRequest ) {
		final AccessValues access = workflowAccessService.getAccessDetails(accessRequest);

		return ResponseEntity.ok(access);
	}
}
