package com.maxar.access.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown to indicate the sensor type specified was not allowed.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedSensorTypeException extends
		Exception
{
	private static final long serialVersionUID = 989732520521578491L;

	/**
	 * @param sensorType The sensor type that was specified.
	 */
	public UnsupportedSensorTypeException(final String sensorType) {
		super("Unsupported sensor type, " + sensorType);
	}
}
