package com.maxar.alert.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when an invalid request is received.
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidRequestException extends
		Exception
{
	private static final long serialVersionUID = -3488210429966259397L;

	/**
	 * @param error
	 *            The error message to display.
	 */
	public InvalidRequestException(
			final String error ) {
		super(
				error);
	}
}
