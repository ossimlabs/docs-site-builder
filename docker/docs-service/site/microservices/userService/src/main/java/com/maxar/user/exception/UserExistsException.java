package com.maxar.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user is to be created, but that user already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class UserExistsException extends
		Exception
{
	private static final long serialVersionUID = 8313019390223687035L;

	/**
	 * @param user
	 *            The user that already exists.
	 */
	public UserExistsException(
			final String user ) {
		super(
				"User already exists: " + user);
	}
}
