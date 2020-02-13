package com.maxar.alert.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlertIdExistsException extends
		Exception
{
	private static final long serialVersionUID = 9065017846251641730L;

	/**
	 * @param id
	 *            The alert ID that already exists in the database.
	 */
	public AlertIdExistsException(
			final String id ) {
		super(
				"Alert with ID " + id + " already exists");
	}
}
