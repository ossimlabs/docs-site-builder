package com.maxar.asset.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to add an asset to the database,
 * but an asset with that ID already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class AssetIdExistsException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id The asset ID that exists in the database.
	 */
	public AssetIdExistsException(final String id) {
		super("Asset with ID " + id + " already exists");
	}
}
