package com.maxar.alert.czml;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.alert.model.Event;
import com.maxar.common.czml.CzmlTypeHandler;

public class AlertCzmlTypeHandler implements
		CzmlTypeHandler
{
	@Autowired
	private AlertCzmlProducer alertCzmlProducer;

	@Override
	public boolean canHandle(
			final Class<?> clazz ) {
		return Event.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean handlesIterable() {
		return false;
	}

	@Override
	public List<String> handle(
			final Object object ) {
		return Collections.singletonList(alertCzmlProducer.produceCzml((Event) object));
	}
}
