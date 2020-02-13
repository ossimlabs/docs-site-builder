package com.maxar.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when there is a problem processing user preferences.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UserPreferencesProcessingException extends
		Exception
{
	private static final long serialVersionUID = 2568875884501809824L;

	public UserPreferencesProcessingException(
			final String user ) {
		super(
				"Error processing user preferences for: " + user);
	}
}
