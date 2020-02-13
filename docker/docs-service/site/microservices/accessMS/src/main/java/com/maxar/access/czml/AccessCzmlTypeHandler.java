package com.maxar.access.czml;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.access.model.UntrimmedAccess;
import com.maxar.access.model.czml.AccessCzmlProperties;
import com.maxar.common.czml.CzmlTypeHandler;

public class AccessCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	AccessCzmlProperties properties;

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return UntrimmedAccess.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return false;
	}

	@Override
	public List<String> handle(
			final Object object ) {
		return Collections.singletonList(((UntrimmedAccess) object).produceCzml(properties));
	}
}
