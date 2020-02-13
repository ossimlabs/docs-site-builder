package com.maxar.access.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.maxar.access.common.service.SupportingServiceClient;
import com.radiantblue.analytics.core.constraint.IAccessConstraint;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.isr.core.component.accgen.IAccess;
import com.radiantblue.analytics.isr.core.component.accgen.ITrimmedAccessConstraintProvider;
import com.radiantblue.analytics.isr.core.model.sensormode.ISensorMode;

public class AccessTrimmingConstraintProvider implements
		ITrimmedAccessConstraintProvider
{

	private static Logger logger = SourceLogger
			.getLogger(
					SupportingServiceClient.class.getName());

	private Map<String, List<IAccessConstraint>> assetConstraints = null;
	private List<IAccessConstraint> userConstraints = null;

	public AccessTrimmingConstraintProvider(
			final Map<String, List<IAccessConstraint>> assetConstraints,
			final List<IAccessConstraint> userConstraints ) {
		this.assetConstraints = assetConstraints;
		this.userConstraints = userConstraints;
	}

	@Override
	public List<IAccessConstraint> getAccessConstraints(
			final IAccess a ) {

		final List<IAccessConstraint> constraints = new ArrayList<>();
		constraints
				.addAll(
						userConstraints);

		if ((assetConstraints != null) && !assetConstraints.isEmpty()) {

			final String assetName;

			if (a.source() instanceof ISensorMode) {
				assetName = ((ISensorMode) a.source()).asset().getName();
			}
			else {
				logger
						.error(
								"Unable to find asset constraints because of a.source()");
				assetName = null;
			}

			final List<IAccessConstraint> ac = assetConstraints
					.get(
							assetName);
			if (ac != null) {
				constraints
						.addAll(
								ac);
			}
			else {
				logger
						.debug(
								"No asset-specific constraints: " + assetName);
			}
		}

		return constraints;
	}
}
