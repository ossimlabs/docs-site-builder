package com.maxar.alert.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to access an alert by ID, but no
 * alert with that ID exists in the database.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AlertIdDoesNotExistException extends
		Exception
{
	private static final long serialVersionUID = -1724500030779056484L;

	/**
	 * @param id
	 *            The alert ID that does not exist in the database.
	 */
	public AlertIdDoesNotExistException(
			final String id ) {
		super(
				"Alert with ID " + id + " does not exist");
	}
}
