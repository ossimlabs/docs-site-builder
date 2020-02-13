package com.maxar.asset.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to update an existing asset, but
 * the ID for the asset in the XML does not match the ID the user supplied.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AssetIdDoesNotMatchException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id The asset ID requested by the user.
	 * @param xmlId The asset ID that was parsed from the XML model.
	 */
	public AssetIdDoesNotMatchException(final String id,
										final String xmlId) {
		super("Asset with ID " + id + " does not match asset ID parsed from XML, " + xmlId);
	}
}
