package com.maxar.target.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to query targets but doesn't include at least
 * one required parameter.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TargetQueryNoParametersException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param parameters The list of parameters
	 */
	public TargetQueryNoParametersException(final String parameters) {
		super("Target query must use at least one of following parameters: " + parameters);
	}
}
