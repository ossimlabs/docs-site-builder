package com.maxar.asset.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to access a sensor mode by name,
 * but that sensor mode name does not exist on that asset.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SensorModeNameDoesNotExistException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param sensorModeName
	 *            The sensor mode name that does not exist on the asset.
	 *
	 * @param assetName
	 *            The name of the asset that does not have that sensor mode name.
	 */
	public SensorModeNameDoesNotExistException(
			final String sensorModeName,
			final String assetName ) {
		super(
				"Sensor mode " + sensorModeName + " does not exist on asset " + assetName);
	}
}
