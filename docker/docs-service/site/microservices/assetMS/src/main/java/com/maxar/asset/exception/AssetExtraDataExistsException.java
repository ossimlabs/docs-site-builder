package com.maxar.asset.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to add extra data to an asset, but
 * extra data with the same name already exists.
 */
@ResponseStatus(code = HttpStatus.CONFLICT)
public class AssetExtraDataExistsException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id The asset ID.
	 * @param name The name of the extra data that already exists for the asset.
	 */
	public AssetExtraDataExistsException(final String id,
										 final String name) {
		super("Asset with ID " + id + " already contains extra data with name " + name);
	}
}
