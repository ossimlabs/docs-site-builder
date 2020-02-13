package com.maxar.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown if a session was requested that does not exist.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SessionNotFoundException extends
		Exception
{
	private static final long serialVersionUID = -588507435780395493L;

	/**
	 * @param user
	 *            The user that does not contain the specified session.
	 * @param session
	 *            The session that was not found for the user.
	 */
	public SessionNotFoundException(
			final String user,
			final String session ) {
		super(
				"Session " + session + " does not exist for user " + user);
	}
}
