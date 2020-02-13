package com.maxar.workflow.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.maxar.access.model.AccessConstraint;
import com.maxar.access.model.AccessValues;
import com.maxar.common.exception.BadRequestException;
import com.maxar.workflow.model.Access;
import com.maxar.workflow.model.AccessDetailsRequest;
import com.maxar.workflow.model.AccessTarget;
import com.maxar.workflow.model.Target;
import com.maxar.workflow.model.TargetAccessRequest;
import com.maxar.workflow.model.TargetGeometryAccessRequest;
import com.maxar.workflow.utils.AccessConversions;
import com.maxar.workflow.utils.TargetConversions;

@Component
public class WorkflowAccessService
{
	@Autowired
	private ApiService apiService;

	/**
	 * Gets a list of valid access constraint names.
	 *
	 * @return The list of valid access constraint names.
	 */
	public List<String> getAccessConstraintNames() {
		return apiService.getAccessConstraintNames();
	}

	/**
	 * Generate accesses for a list of targets and assets.
	 *
	 * @param accessRequest
	 *            The list of target IDs and asset IDs to generate accesses for.
	 * @return The list of accesses generated.
	 */
	public List<Access> getAccessesForTargetsAndAssets(
			final TargetAccessRequest accessRequest ) {
		return Optional.ofNullable(accessRequest.getTargetIds())
				.orElseThrow(() -> new BadRequestException(
						"Target ID list cannot be null"))
				.stream()
				.map(apiService::getTargetById)
				.flatMap(Optional::stream)
				.map(TargetConversions::targetMsToWorkflow)
				.map(target -> generateAccessesForTarget(	target,
															accessRequest.getSpaceAssetIds(),
															accessRequest.getStart(),
															accessRequest.getStop(),
															accessRequest.getAccessConstraints(),
															accessRequest.getAssetStartTimeBufferMs(),
															accessRequest.getAssetEndTimeBufferMs()))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	/**
	 * Generate accesses for a list of target IDs and geometries.
	 *
	 * @param accessRequest
	 *            The access generation parameters.
	 * @return The list of accesses generated.
	 */
	public List<Access> getAccessesForTargetGeometriesAndAssets(
			final TargetGeometryAccessRequest accessRequest ) {
		return Optional.ofNullable(accessRequest.getTargets())
				.orElseThrow(() -> new BadRequestException(
						"Targets list cannot be null"))
				.stream()
				.map(target -> generateAccessesForGeometry(	target,
															accessRequest.getSpaceAssetIds(),
															accessRequest.getStart(),
															accessRequest.getStop(),
															accessRequest.getAccessConstraints(),
															accessRequest.getAssetStartTimeBufferMs(),
															accessRequest.getAssetEndTimeBufferMs()))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves the list of access details for access and time specified in an
	 * AccessDetailsRequest
	 *
	 * @param accessDetailsRequest - contains all input params
	 *
	 * @return AccessValues - The access details for the request
	 */
	public AccessValues getAccessDetails(
			final AccessDetailsRequest accessDetailsRequest ) {
		return apiService.getAccessDetails(accessDetailsRequest);
	}

	/**
	 * Generate accesses for a target during an event's time window.
	 *
	 * @param target
	 *            The target to access.
	 * @param spaceAssetIds
	 *            The list of space asset names to generate accesses for.
	 * @param start
	 *            The start time provided by UI to generate accesses for
	 * @param stop
	 *            The stop time provided by UI to generate accesses for
	 * @param accessConstraints
	 *            The access constraints provided by UI to generate accesses for
	 * @param assetStartTimeBufferMs
	 *            The time in milliseconds to pad the start of the access for asset
	 *            path generation in CZML
	 * @param assetEndTimeBufferMs
	 *            The time in milliseconds to pad the end of the access for asset
	 *            path generation in CZML
	 * @return A list of accesses for the target during the time window.
	 */
	private List<Access> generateAccessesForTarget(
			final Target target,
			final List<String> spaceAssetIds,
			final String start,
			final String stop,
			final List<AccessConstraint> accessConstraints,
			final Long assetStartTimeBufferMs,
			final Long assetEndTimeBufferMs ) {
		final AccessTarget accessTarget = new AccessTarget();
		accessTarget.setTargetId(target.getTargetId());
		accessTarget.setGeometry(target.getGeometryWkt());
		accessTarget.setTargetName(target.getTargetName());
		accessTarget.setCountryCode(target.getCountryCode());
		accessTarget.setGeoRegion(target.getGeoRegion());

		return generateAccessesForGeometry(	accessTarget,
											spaceAssetIds,
											start,
											stop,
											accessConstraints,
											assetStartTimeBufferMs,
											assetEndTimeBufferMs);
	}

	/**
	 * Generate accesses for a geometry and set the target ID for each generated
	 * access.
	 *
	 * @param accessTarget
	 *            The target to generate access for.
	 * @param spaceAssetIds
	 *            The list of space asset names to generate accesses for.
	 * @param start
	 *            The start time provided by UI to generate accesses for
	 * @param stop
	 *            The stop time provided by UI to generate accesses for
	 * @param accessConstraints
	 *            The access constraints provided by UI to generate accesses for
	 * @param assetStartTimeBufferMs
	 *            The time in milliseconds to pad the start of the access for asset
	 *            path generation in CZML
	 * @param assetEndTimeBufferMs
	 *            The time in milliseconds to pad the end of the access for asset
	 *            path generation in CZML
	 * @return A list of accesses for the target during the time window.
	 */
	private List<Access> generateAccessesForGeometry(
			final AccessTarget accessTarget,
			final List<String> spaceAssetIds,
			final String start,
			final String stop,
			final List<AccessConstraint> accessConstraints,
			final Long assetStartTimeBufferMs,
			final Long assetEndTimeBufferMs ) {
		return generateSpaceAccessesForGeometry(accessTarget,
												spaceAssetIds,
												start,
												stop,
												accessConstraints,
												assetStartTimeBufferMs,
												assetEndTimeBufferMs);
	}

	/**
	 * Generate accesses for a geometry during an event's time window using the
	 * space access service.
	 *
	 * @param accessTarget
	 *            The target to generate access for.
	 * @param spaceAssetIds
	 *            The list of space asset names to generate accesses for.
	 * @param start
	 *            The start time provided by UI to generate accesses for
	 * @param stop
	 *            The stop time provided by UI to generate accesses for
	 * @param accessConstraints
	 *            The access constraints provided by UI to generate accesses for
	 * @param assetStartTimeBufferMs
	 *            The time in milliseconds to pad the start of the access for asset
	 *            path generation in CZML
	 * @param assetEndTimeBufferMs
	 *            The time in milliseconds to pad the end of the access for asset
	 *            path generation in CZML
	 * @return A list of accesses for the geometry during the time window, or an
	 *         empty list if no space assets were specified.
	 */
	private List<Access> generateSpaceAccessesForGeometry(
			final AccessTarget accessTarget,
			final List<String> spaceAssetIds,
			final String start,
			final String stop,
			final List<AccessConstraint> accessConstraints,
			final Long assetStartTimeBufferMs,
			final Long assetEndTimeBufferMs ) {
		if (spaceAssetIds.isEmpty()) {
			return Collections.emptyList();
		}

		final List<Access> accesses = apiService.getSpaceAccessesByGeometryAndTimes(accessTarget.getGeometry(),
																					start,
																					stop,
																					spaceAssetIds,
																					accessConstraints,
																					assetStartTimeBufferMs,
																					assetEndTimeBufferMs)

				.stream()
				.map(AccessConversions::accessMsToWorkflow)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		accesses.forEach(access -> access.setDuration(new Duration(access.getStartTime(),
																   access.getEndTime()).getStandardSeconds()));

		accesses.forEach(access -> setTargetFieldsOnAccess(	access,
															accessTarget));
		accesses.forEach(access -> access.setStartDetails(generateSpaceAccessDetails(	access,
																						access.getStartTime(),
																						accessTarget.getGeometry())));
		accesses.forEach(access -> access.setTcaDetails(generateSpaceAccessDetails(	access,
																					access.getTcaTime(),
																					accessTarget.getGeometry())));
		accesses.forEach(access -> access.setStopDetails(generateSpaceAccessDetails(access,
																					access.getEndTime(),
																					accessTarget.getGeometry())));

		return accesses.stream()
				.map(access -> generateSpaceAccessTcaDetails(	access,
																accessTarget.getGeometry()))
				.collect(Collectors.toList());
	}

	/**
	 * Add the time of closest approach (TCA) details to a space asset access.
	 *
	 * @param access
	 *            The input access details.
	 * @param geometryWkt
	 *            The geometry of the access target.
	 * @return The access with the TCA azimuth, TCA elevation, and TCA quality
	 *         fields set.
	 */
	private Access generateSpaceAccessTcaDetails(
			final Access access,
			final String geometryWkt ) {
		final AccessValues accessValues = apiService.getSpaceAccessDetails(	access.getAssetId(),
																			access.getTcaTime(),
																			access.getPropagatorType(),
																			access.getSensorMode(),
																			geometryWkt);

		access.setAzimuth(accessValues.getAzimuthDeg());
		access.setElevation(accessValues.getElevationDeg());
		access.setQuality(accessValues.getQuality());

		return access;
	}

	/**
	 * Returns the access details based on start/stop/tca time and target's geometry
	 * for weather
	 *
	 * @param access
	 *            The input access details.
	 * @param time
	 *            The start/stop/tca time
	 * @param geometryWkt
	 *            The target's geometry
	 * @return The access details based on access parameters and provided time
	 */
	private AccessValues generateSpaceAccessDetails(
			final Access access,
			final DateTime time,
			final String geometryWkt ) {
		final AccessValues accessValues = apiService.getSpaceAccessDetails(	access.getAssetId(),
																			time,
																			access.getPropagatorType(),
																			access.getSensorMode(),
																			geometryWkt);

		accessValues.setCloudCoverPct(apiService
				.getAccessWeather(apiService.generateWeatherByDateAndGeometryRequest(	time.toString(),
																						geometryWkt)));
		accessValues.setSlantRangeKms(accessValues.getSlantRangeMeters()/1000);

		return accessValues;
	}

	/**
	 * Set the country code, georegion, and target name fields on the access from
	 * the target.
	 *
	 * @param access
	 *            The access to modify.
	 * @param target
	 *            The target information.
	 */
	private static void setTargetFieldsOnAccess(
			final Access access,
			final AccessTarget target ) {
		access.setTargetId(target.getTargetId());
		access.setTargetName(target.getTargetName());
		access.setCountryCode(target.getCountryCode());
		access.setGeoRegion(target.getGeoRegion());
	}
}