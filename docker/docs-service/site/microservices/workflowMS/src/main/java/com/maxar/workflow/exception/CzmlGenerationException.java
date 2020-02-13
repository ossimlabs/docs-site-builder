package com.maxar.workflow.exception;

/**
 * Thrown if there was an error generating CZML.
 */
public class CzmlGenerationException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param error
	 *            The error message describing the failure generating CZML.
	 */
	public CzmlGenerationException(
			final String error ) {
		super(
				error);
	}
}
