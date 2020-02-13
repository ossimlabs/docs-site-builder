package com.maxar.asset.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to add an asset to the database,
 * but an asset with that name already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class AssetNameExistsException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param name The asset name that exists in the database.
	 */
	public AssetNameExistsException(final String name) {
		super("Asset with name " + name + " already exists");
	}
}
