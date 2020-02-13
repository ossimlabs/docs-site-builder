package com.maxar.common.csv;

import java.util.List;

public interface CsvTypeHandler<A>
{
	public boolean canHandle(
			final Class<? extends Object> clazz );

	public String headers();

	@SuppressWarnings("unchecked")
	default public List<String> handleObject(
			final Object object ) {
		return (handle((A) object));
	}

	public List<String> handle(
			final A object );
}
