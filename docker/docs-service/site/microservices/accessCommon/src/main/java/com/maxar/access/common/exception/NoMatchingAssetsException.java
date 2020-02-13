package com.maxar.access.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a sensor type is specified, but none of the specified assets
 * contain that type of sensor.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoMatchingAssetsException extends
		Exception
{
	private static final long serialVersionUID = 5276523024888834119L;

	/**
	 * @param sensorType The sensor type that was specified.
	 */
	public NoMatchingAssetsException(final String sensorType) {
		super("No assets match the provided sensor type, " + sensorType);
	}
}
