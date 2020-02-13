package com.maxar.common.utils;

import com.maxar.common.exception.BadRequestException;

public class PaginationParameterValidator
{
	private PaginationParameterValidator() {}

	/**
	 * Check the page and count request parameters.
	 *
	 * If the page number is null or less than 0, or if the count is null or less
	 * than or equal to 0, then a BadRequestException is thrown.
	 *
	 * @param page
	 *            The page number request parameter, which must be 0 or greater.
	 * @param count
	 *            The count request parameter, which must be greater than 0.
	 */
	public static void validatePageAndCountParameters(
			final Integer page,
			final Integer count ) {
		if (page == null || page < 0) {
			throw new BadRequestException(
					"Page request parameter must be >= 0");
		}
		if (count == null || count <= 0) {
			throw new BadRequestException(
					"Count request parameter must be > 0");
		}
	}
}
