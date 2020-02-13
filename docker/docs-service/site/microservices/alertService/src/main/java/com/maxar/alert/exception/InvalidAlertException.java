package com.maxar.alert.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to create an alert that is
 * invalid.
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidAlertException extends
		Exception
{
	private static final long serialVersionUID = 6474062181841660850L;

	public InvalidAlertException() {
		super(
				"Invalid alert");
	}
}
