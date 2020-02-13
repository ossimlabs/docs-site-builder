package com.maxar.asset.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a user tries to access a mission by id for an
 * asset at a time, but no mission with that id exists in the database.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MissionIdDoesNotExistException extends
		Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param missionId
	 *            The mission id that does not exist in the database.
	 *
	 * @param assetName
	 *            The name of the asset that does not have that mission id.
	 *
	 * @param atTime
	 *            The time that the asset that does not have that mission id.
	 */
	public MissionIdDoesNotExistException(
			final String missionId,
			final String assetName,
			final String atTime ) {
		super(
				"Mission id " + missionId + " does not exist for asset " + assetName + " at time " + atTime);
	}
}
