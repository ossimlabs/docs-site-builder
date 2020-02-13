package com.maxar.asset.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to access an asset by name, but no
 * asset with that name exists in the database.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AssetNameDoesNotExistException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param name The asset name that does not exist in the database.
	 */
	public AssetNameDoesNotExistException(final String name) {
		super("Asset with name " + name + " does not exist");
	}
}
