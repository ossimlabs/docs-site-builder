package com.maxar.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a session is to be added to a user, but that user already has a
 * session with that ID.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class SessionExistsException extends
		Exception
{
	private static final long serialVersionUID = 6674391180183255363L;

	/**
	 * @param user
	 *            The user that contains the session already.
	 * @param session
	 *            The session that already exists for the user.
	 */
	public SessionExistsException(
			final String user,
			final String session ) {
		super(
				"Session " + session + " already exists for user " + user);
	}
}
