package com.maxar.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown if a user was requested that does not exist.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends
		Exception
{
	private static final long serialVersionUID = -3220857707566882364L;

	/**
	 * @param user
	 *            The user that was not found.
	 */
	public UserNotFoundException(
			final String user ) {
		super(
				"User not found: " + user);
	}
}
