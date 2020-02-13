package com.maxar.asset.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to access an asset by ID, but no
 * asset with that ID exists in the database.
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class AssetIdDoesNotExistException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id The asset ID that does not exist in the database.
	 */
	public AssetIdDoesNotExistException(final String id) {
		super("Asset with ID " + id + " does not exist");
	}
}
