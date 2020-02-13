package com.maxar.asset.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to use a propagator type, but no
 * propagator type with that name exists in rb-analytics.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PropagatorTypeDoesNotExistException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param propagatorType
	 *            The propagator type that does not exist in rb-analytics.
	 */
	public PropagatorTypeDoesNotExistException(
			final String propagatorType ) {
		super(
				"Propagator type " + propagatorType + " does not exist");
	}
}
