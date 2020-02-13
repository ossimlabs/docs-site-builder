package com.maxar.workflow.utils;

import org.joda.time.DateTime;

import com.maxar.target.model.TargetModel;
import com.maxar.workflow.model.Target;

public class TargetConversions
{
	private TargetConversions() {}

	/**
	 * Convert a TargetModel from the Target service to a Workflow service Target.
	 *
	 * @param targetMsTarget
	 *            A Target service target.
	 * @return The converted Workflow service target.
	 */
	public static Target targetMsToWorkflow(
			final TargetModel targetMsTarget,
			final DateTime startTime,
			final DateTime stoptime ) {
		final Target workflowTarget = new Target();
		workflowTarget.setTargetId(targetMsTarget.getTargetId());
		workflowTarget.setTargetName(targetMsTarget.getTargetName());
		workflowTarget.setCountryCode(targetMsTarget.getCountryCode());
		workflowTarget.setGeoRegion(targetMsTarget.getGeoRegion());
		workflowTarget.setGeometryWkt(targetMsTarget.getGeometry()
				.toText());
		workflowTarget.setCentroidWkt(targetMsTarget.getGeometry()
				.getCentroid()
				.toText());
		workflowTarget.setEstimated(targetMsTarget.isEstimated());
		workflowTarget.setEventStart(startTime);
		workflowTarget.setEventStop(stoptime);

		return workflowTarget;
	}

	public static Target targetMsToWorkflow(
			final TargetModel targetMsTarget ) {
		return targetMsToWorkflow(	targetMsTarget,
									null,
									null);
	}
}
