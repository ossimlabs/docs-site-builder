package com.maxar.workflow.controller;

import static com.maxar.common.utils.PaginationParameterValidator.validatePageAndCountParameters;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.spaceobjectcatalog.model.SpaceObject;
import com.maxar.workflow.model.EphemeridesRequest;
import com.maxar.workflow.service.WorkflowSpaceObjectCatalogService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/catalog")
public class WorkflowSpaceObjectCatalogController
{
	@Autowired
	private WorkflowSpaceObjectCatalogService workflowSpaceObjectCatalogService;

	@CrossOrigin
	@PostMapping(value = "/asset/ephemerides")
	@ApiOperation("Retrieves the ephemerides for the given assets")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The ephemerides were successfully retrieved"),
		@ApiResponse(code = 400, message = "There was a problem with the ephemerides request"),
		@ApiResponse(code = 404, message = "There are no ephemerides for the requested assets")
	})
	public ResponseEntity<List<SpaceObject>> getEphemeridesForAssets(
			@ApiParam(value = "The input parameters to generate the ephemerides for requested assets")
			@RequestBody
			final EphemeridesRequest ephemeridesRequest ) {

		validatePageAndCountParameters(	ephemeridesRequest.getPage(),
										ephemeridesRequest.getCount());

		final List<SpaceObject> spaceObjects = workflowSpaceObjectCatalogService
				.getEphemeridesByAssetName(ephemeridesRequest);

		final SpaceObject removalSpaceObjectEmpty = new SpaceObject();
		removalSpaceObjectEmpty.setScn(0);
		removalSpaceObjectEmpty.setEphemerides(Collections.emptyList());

		// Remove space objects that weren't found in the catalog
		spaceObjects.removeAll(Collections.singletonList(removalSpaceObjectEmpty));

		return ResponseEntity.ok(spaceObjects);
	}
}