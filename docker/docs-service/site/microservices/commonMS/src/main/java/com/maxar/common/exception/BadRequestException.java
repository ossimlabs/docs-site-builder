package com.maxar.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown to return an HTTP BAD_REQUEST (400) error with a
 * descriptive message.
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BadRequestException extends
		RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param message The message to describe how the request was incorrect.
	 */
	public BadRequestException(final String message) {
		super(message);
	}
}
