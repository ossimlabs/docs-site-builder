package com.maxar.workflow.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import com.maxar.access.model.UntrimmedAccess;
import com.maxar.workflow.model.Access;

public class AccessConversions
{
	private AccessConversions() {}

	/**
	 * Convert an UntrimmedAccess from the Access service to a list of Workflow
	 * service Accesses with type "Untrimmed".
	 *
	 * For each trimmed access for the untrimmed access, a Workflow service Access with type "Trimmed"
	 * will be created
	 *
	 * @param accessMsAccess
	 *            An Access service access.
	 * @return The converted Workflow service accesses.
	 */
	public static List<Access> accessMsToWorkflow(
			final UntrimmedAccess accessMsAccess ) {

		final Access.AccessBuilder unTrimmedAccessBuilder = Access.builder()
				.startTime(DateTime.parse(accessMsAccess.getStartTimeISO8601()))
				.endTime(DateTime.parse(accessMsAccess.getEndTimeISO8601()))
				.tcaTime(DateTime.parse(accessMsAccess.getTcaTimeISO8601()))
				.assetId(accessMsAccess.getAssetName())
				.failureReason(accessMsAccess.getFailureReason())
				.propagatorType(accessMsAccess.getPropagatorType())
				.sensorMode(accessMsAccess.getSensorMode())
				.czml(accessMsAccess.getCzml())
				.type("Untrimmed")
				.assetName(accessMsAccess.getAssetName())
				.sensorType(accessMsAccess.getSensorType())
				.pass(accessMsAccess.getPass());

		List<Access> accesses = new ArrayList<>(Arrays.asList(unTrimmedAccessBuilder.build()));


		List<Access> trimmedAccesses = accessMsAccess.getTrimmedAccesses()
				.stream()
				.map(trimmedAccess -> unTrimmedAccessBuilder
						.startTime(DateTime.parse(trimmedAccess.getStartTimeISO8601()))
						.endTime(DateTime.parse(trimmedAccess.getEndTimeISO8601()))
						.tcaTime(DateTime.parse(trimmedAccess.getTcaTimeISO8601()))
						.czml(trimmedAccess.getCzml())
						.type("Trimmed")
						.build())
				.collect(Collectors.toList());

		accesses.addAll(trimmedAccesses);
		return accesses;

	}
}
