package com.maxar.common.czml;

import java.util.List;

public interface CzmlTypeHandler
{
	public boolean canHandle(
			final Class<? extends Object> clazz );

	public boolean handlesIterable();

	public List<String> handle(
			final Object object );
}
