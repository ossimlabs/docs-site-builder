package com.maxar.geometric.intersection.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to create an area of interest that
 * is invalid.
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidAreaOfInterestException extends
		RuntimeException
{
	private static final long serialVersionUID = 1L;

	public InvalidAreaOfInterestException() {
		super(
				"Invalid area of interest");
	}

	public InvalidAreaOfInterestException(
			final String message ) {
		super(
				"Invalid area of interest: " + message);
	}
}
