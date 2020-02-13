package com.maxar.opgen.czml;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.opgen.model.Op;
import com.maxar.opgen.model.czml.OpCzmlProperties;

public class OpCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	OpCzmlProperties properties;

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return Op.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return false;
	}

	@Override
	public List<String> handle(
			final Object object ) {
		return Collections.singletonList(((Op) object).produceCzml(properties));
	}

}
