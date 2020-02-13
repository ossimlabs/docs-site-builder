package com.maxar.opgen.types;

import org.joda.time.DateTime;

import com.radiantblue.analytics.isr.core.component.accgen.IAccessRequest;
import com.radiantblue.analytics.isr.core.component.accgen.ValidAccess;

public class DummyValidAccess extends
		ValidAccess
{

	public DummyValidAccess(
			final DateTime startTime,
			final DateTime endTime,
			final IAccessRequest accessRequest ) {
		super(
				startTime,
				endTime,
				accessRequest);
	}
}
