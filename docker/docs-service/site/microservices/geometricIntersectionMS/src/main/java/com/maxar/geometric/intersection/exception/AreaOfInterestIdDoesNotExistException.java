package com.maxar.geometric.intersection.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to access an area of interest by
 * ID, but no area of interest with that ID exists in the database.
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class AreaOfInterestIdDoesNotExistException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 *            The area of interest ID that does not exist in the database.
	 */
	public AreaOfInterestIdDoesNotExistException(
			final String id ) {
		super(
				"Area of interest with ID " + id + " does not exist");
	}
}
