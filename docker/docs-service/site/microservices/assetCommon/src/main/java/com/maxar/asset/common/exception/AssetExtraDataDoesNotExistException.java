package com.maxar.asset.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a super tries to update extra data for an
 * asset, but the asset does not contain extra data with that name.
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class AssetExtraDataDoesNotExistException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id The asset ID.
	 * @param name The name of the extra data that does not exist for the asset.
	 */
	public AssetExtraDataDoesNotExistException(final String id,
											   final String name) {
		super("Asset with ID " + id + " does not contain extra data with name " + name);
	}
}
