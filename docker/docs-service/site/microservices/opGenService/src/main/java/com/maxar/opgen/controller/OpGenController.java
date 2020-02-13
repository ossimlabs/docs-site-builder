package com.maxar.opgen.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.asset.common.client.airborne.AssetRetrieverAirborne;
import com.maxar.asset.common.client.space.AssetRetrieverSpace;
import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.exception.AssetNameDoesNotExistException;
import com.maxar.asset.common.exception.MissionIdDoesNotExistException;
import com.maxar.asset.common.exception.NoEphemerisFoundException;
import com.maxar.asset.common.exception.NoMissionsFoundException;
import com.maxar.asset.common.exception.PropagatorTypeDoesNotExistException;
import com.maxar.asset.common.exception.SensorModeNameDoesNotExistException;
import com.maxar.asset.common.service.ApiService;
import com.maxar.opgen.model.Op;
import com.maxar.opgen.model.OpAirborneRequest;
import com.maxar.opgen.model.OpSpaceRequest;
import com.maxar.opgen.service.OpGenService;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.isr.core.model.asset.Asset;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/opgen")
@Api
public class OpGenController
{
	private static Logger logger = SourceLogger.getLogger(OpGenController.class.getName());

	@Autowired
	protected ApiService apiService;

	@Autowired
	private OpGenService opGenService;

	@Autowired
	private AssetRetrieverSpace spaceClient;

	@Autowired
	private AssetRetrieverAirborne airborneClient;

	@CrossOrigin
	@PostMapping(value = "/space")
	@ApiOperation("Get the requested op details")
	@ApiResponses({
		@ApiResponse(code = 201, message = "The op was successfully created"),
		@ApiResponse(code = 400, message = "There was a problem with the request"),
		@ApiResponse(code = 404, message = "The space asset or some part of it does not exist")
	})
	public ResponseEntity<List<Op>> createOpSpace(
			@RequestBody
			final OpSpaceRequest opRequest )
			throws AssetNameDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			PropagatorTypeDoesNotExistException,
			SensorModeNameDoesNotExistException,
			NoEphemerisFoundException {
		logger.info("Received request Asset: " + opRequest.getAssetName() + " Sensor mode: "
				+ opRequest.getSensorModeName() + " Propagator: " + opRequest.getPropagatorType() + " for time: "
				+ opRequest.getStartTime()
						.toString()
				+ " Target: " + opRequest.getTargetGeometryWkt());

		// Get a legit asset from AssetCommon
		final Asset asset = spaceClient.getAssetModelByName(opRequest.getAssetName());

		apiService.updateAssetEphemeris(asset,
										opRequest.getStartTime(),
										opRequest.getPropagatorType());

		final List<Op> ops = opGenService.createOpAtTime(	opRequest,
															asset);
		return new ResponseEntity<>(
				ops,
				HttpStatus.OK);
	}

	@CrossOrigin
	@PostMapping(value = "/airborne")
	@ApiOperation("Get the requested op details")
	@ApiResponses({
		@ApiResponse(code = 201, message = "The op was successfully created"),
		@ApiResponse(code = 400, message = "There was a problem with the request"),
		@ApiResponse(code = 404, message = "The airborne asset or some part of it does not exist")
	})
	public ResponseEntity<List<Op>> createOpAirborne(
			@RequestBody
			final OpAirborneRequest opRequest )
			throws AssetNameDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			SensorModeNameDoesNotExistException,
			NoMissionsFoundException,
			MissionIdDoesNotExistException {
		logger.info("Received request Asset: " + opRequest.getAssetName() + " Sensor mode: "
				+ opRequest.getSensorModeName() + " Mission id: " + opRequest.getMissionId() + " for time: "
				+ opRequest.getStartTime()
						.toString()
				+ " Target: " + opRequest.getTargetGeometryWkt());

		// Get a legit asset from AssetCommon
		final Asset asset = airborneClient.getAssetModelByName(opRequest.getAssetName());

		if (opRequest.getEndTime() == null) {
			apiService.updateAssetMission(	asset,
											opRequest.getStartTime(),
											opRequest.getMissionId());
		}
		else {
			apiService.updateAssetMission(	asset,
											opRequest.getStartTime(),
											opRequest.getEndTime(),
											opRequest.getMissionId());
		}

		final List<Op> ops = opGenService.createOpAtTime(	opRequest,
															asset);

		return new ResponseEntity<>(
				ops,
				HttpStatus.OK);
	}
}
