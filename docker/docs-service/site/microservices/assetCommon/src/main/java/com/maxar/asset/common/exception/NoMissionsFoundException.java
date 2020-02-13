package com.maxar.asset.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to get the missions for an asset
 * by name for a specific time, but no missions for that asset exist in the
 * database.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoMissionsFoundException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param assetName
	 *            The name of the asset that has no missions in the database.
	 *
	 * @param atTime
	 *            The time that no missions were found.
	 */
	public NoMissionsFoundException(
			final String assetName,
			final String atTime ) {
		super(
				"No missions were found for asset " + assetName + " at time " + atTime);
	}
}
