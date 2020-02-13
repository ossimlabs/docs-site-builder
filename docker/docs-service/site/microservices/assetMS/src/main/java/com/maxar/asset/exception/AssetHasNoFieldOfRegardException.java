package com.maxar.asset.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to get a field of regard for an
 * asset, but that asset does not have the appropriate constructs to get a field
 * of regard.
 */
@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
public class AssetHasNoFieldOfRegardException extends
		RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id The asset ID.
	 */
	public AssetHasNoFieldOfRegardException(final String id) {
		super("Asset with ID " + id + " has no field of regard");
	}
}
