package com.maxar.asset.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to get the ephemeris for an asset
 * by name for a specific time, but no ephemeris for that asset exists in the
 * database.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoEphemerisFoundException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param assetName
	 *            The name of the asset that has no ephemeris in the database.
	 *
	 * @param atTime
	 *            The time that no ephemeris were found.
	 */
	public NoEphemerisFoundException(
			final String assetName,
			final String atTime ) {
		super(
				"No ephemeris (TLEs) was found for asset " + assetName + " at time " + atTime);
	}
}
