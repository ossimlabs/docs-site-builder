package com.maxar.access.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.access.common.exception.NoMatchingAssetsException;
import com.maxar.access.common.exception.UnsupportedSensorTypeException;
import com.maxar.access.common.service.AccessService;
import com.maxar.access.common.service.SupportingServiceClient;
import com.maxar.access.common.utils.AssetUtils;
import com.maxar.access.common.utils.ConstraintUtils;
import com.maxar.access.model.AccessConstraint;
import com.maxar.access.model.AccessGenerationRequest;
import com.maxar.access.model.AccessValues;
import com.maxar.access.model.UntrimmedAccess;
import com.maxar.asset.common.client.space.AssetRetrieverSpace;
import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.exception.AssetNameDoesNotExistException;
import com.maxar.asset.common.exception.NoEphemerisFoundException;
import com.maxar.asset.common.exception.PropagatorTypeDoesNotExistException;
import com.maxar.common.utils.GeoUtils;
import com.maxar.target.model.OrderOfBattle;
import com.maxar.target.model.TargetModel;
import com.maxar.target.model.TargetType;
import com.radiantblue.analytics.core.constraint.IAccessConstraint;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.isr.core.model.sensormode.ISensorMode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api
public class AccessController
{
	private static Logger logger = SourceLogger.getLogger(AccessController.class.getName());

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	@Autowired
	private AccessService accessService;

	@Autowired
	private SupportingServiceClient serviceClient;

	@Autowired
	private AssetRetrieverSpace assetClient;

	@Autowired
	private ConstraintUtils constraintUtils;

	@CrossOrigin
	@PostMapping(value = "/accesses")
	@ApiOperation("Generates accesses based on target/geo, asset(s), timeframe, and constraints")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The accesses were successfully generated"),
		@ApiResponse(code = 400, message = "The inputs could not be parsed")
	})
	public ResponseEntity<List<UntrimmedAccess>> getAccesses(
			@ApiParam(value = "The input parameters to generate accesses for")
			@RequestBody
			final AccessGenerationRequest requestBody )
			throws UnsupportedSensorTypeException,
			NoMatchingAssetsException,
			AssetNameDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		Geometry geometry;
		TargetModel target;
		TargetType targetType = TargetType.POINT;
		OrderOfBattle orderOfBattle = OrderOfBattle.GROUND;
		try {
			final WKTReader reader = new WKTReader();
			geometry = reader.read(requestBody.getTgtOrGeometryWkt());
		}
		catch (final ParseException e) {
			// Attempt TGT ID lookup if the parameter is not geometry wkt
			target = serviceClient.lookupTarget(requestBody.getTgtOrGeometryWkt());
			if (target == null) {
				logger.error("Unable to parse geometry WKT or lookup target by ID: "
						+ requestBody.getTgtOrGeometryWkt());
				return ResponseEntity.badRequest()
						.build();
			}
			else {
				geometry = target.getGeometry();
				targetType = target.getTargetType();
				orderOfBattle = target.getOrderOfBattle();
			}
		}

		DateTime startTime;
		DateTime endTime;
		try {
			startTime = ISODateTimeFormat.dateTimeParser()
					.parseDateTime(requestBody.getStartTimeISO8601());
			endTime = ISODateTimeFormat.dateTimeParser()
					.parseDateTime(requestBody.getEndTimeISO8601());
		}
		catch (final Exception e) {
			logger.error("Cannot parse ISO8601 datetime Strings: " + requestBody.getStartTimeISO8601() + "/"
					+ requestBody.getEndTimeISO8601());
			return ResponseEntity.badRequest()
					.build();
		}

		List<Asset> assets = new ArrayList<>();
		List<String> requestedIds = requestBody.getAssetIDs();
		if ((requestedIds == null) || requestedIds.isEmpty()) {
			requestedIds = serviceClient.getAllAssetNames();
		}
		for (final String assetID : requestedIds) {
			final Asset asset = assetClient.getAssetModelByName(assetID);
			if (asset != null) {
				assets.add(asset);
			}
			else {
				logger.error("Unable to retrieve asset models for the provided ID: " + assetID);
				return ResponseEntity.badRequest()
						.build();
			}
		}

		final List<IAccessConstraint> constraints = new ArrayList<>();
		for (final AccessConstraint requestConstraint : requestBody.getAccessConstraints()) {
			final IAccessConstraint constraint = constraintUtils.buildConstraint(requestConstraint);
			if (constraint != null) {
				constraints.add(constraint);
			}
			else {
				logger.error("Unsupported or badly formatted constraint: " + requestConstraint.getName());
				return ResponseEntity.badRequest()
						.build();
			}
		}

		assets = AssetUtils.filterAssetListBySensorType(assets,
														requestBody.getSensorType());

		final List<UntrimmedAccess> accesses = accessService.getAccesses(	assets,
																			startTime,
																			endTime,
																			constraints,
																			requestBody.getSensorType(),
																			geometry,
																			targetType,
																			requestBody.getPropagatorType(),
																			orderOfBattle);

		accesses.forEach(untrimmedAccess -> untrimmedAccess
				.setCzmlAssetStartTimeBufferMs(requestBody.getAssetStartTimeBufferMs()));
		accesses.forEach(untrimmedAccess -> untrimmedAccess
				.setCzmlAssetEndTimeBufferMs(requestBody.getAssetEndTimeBufferMs()));

		return ResponseEntity.ok(accesses);
	}

	@CrossOrigin
	@GetMapping(value = "/access-details")
	@ApiOperation("Generate access details based on target/geo, asset, and exact time")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The access values were successfully generated"),
		@ApiResponse(code = 400, message = "The inputs could not be parsed")
	})
	public @ResponseBody ResponseEntity<AccessValues> getDetailsAtTime(
			@RequestParam
			@ApiParam(name = "assetID", value = "Asset ID", example = "RS02")
			final String assetID,
			@RequestParam
			@ApiParam(name = "tgtOrGeometryWKT", value = "Geometry String using well known text (WKT)", example = "POINT(-77.38368 38.96686 135.0)")
			final String tgtOrGeometryWKT,
			@RequestParam
			@ApiParam(name = "date", value = "ISO8601 Formatted Date String", example = "2020-01-01T21:40:17.742Z")
			final String date,
			@RequestParam
			@ApiParam(name = "sensorMode", value = "Sensor Mode Name", example = "RS02_Framing_Mode")
			final String sensorMode,
			@RequestParam
			@ApiParam(name = "propagatorType", value = "Propagator Type", example = "SGP4")
			final String propagatorType )
			throws AssetNameDoesNotExistException,
			AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			PropagatorTypeDoesNotExistException,
			NoEphemerisFoundException {

		Geometry geometry;
		TargetModel target;
		try {
			final WKTReader reader = new WKTReader();
			final Geometry tempGeometry = reader.read(tgtOrGeometryWKT);
			geometry = GeoUtils.convertDegreesToRadians(tempGeometry);
		}
		catch (final ParseException e) {
			// Attempt TGT ID lookup if the parameter is not geometry wkt
			target = serviceClient.lookupTarget(tgtOrGeometryWKT);
			if (target == null) {
				logger.error("Unable to parse geometry WKT or lookup target by ID: " + tgtOrGeometryWKT);
				return ResponseEntity.badRequest()
						.build();
			}
			else {
				geometry = GeoUtils.convertDegreesToRadians(target.getGeometry());
			}
		}

		DateTime atTime;
		try {
			atTime = ISODateTimeFormat.dateTimeParser()
					.parseDateTime(date);
		}
		catch (final Exception e) {
			logger.error("Cannot parse ISO8601 datetime String: " + date);
			return ResponseEntity.badRequest()
					.build();
		}

		final Asset asset = assetClient.getAssetModelByName(assetID);
		if (asset == null) {
			logger.error("Unable to retrieve asset model for the provided ID: " + assetID);
			return ResponseEntity.badRequest()
					.build();
		}

		final ISensorMode iSensorMode = AssetUtils.getSensorModeByName(	asset,
																		sensorMode);
		if (iSensorMode == null) {
			logger.error("Unable to find sensor mode for the provided name: " + sensorMode);
			return ResponseEntity.badRequest()
					.build();
		}

		final AccessValues accessValues = accessService.getDetailsAtTime(	geometry,
																			atTime,
																			asset,
																			iSensorMode,
																			propagatorType);

		return ResponseEntity.ok(accessValues);
	}

	@CrossOrigin
	@GetMapping("/access/constraints")
	@ApiOperation("Returns all constraint names supported for access generation")
	public @ResponseBody ResponseEntity<List<String>> getConstraintNames() {
		final List<String> names = constraintUtils.getAllConstraintNames();
		return ResponseEntity.ok(names);
	}
}
