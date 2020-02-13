package com.maxar.workflow.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.maxar.common.exception.BadRequestException;
import com.maxar.target.model.TargetModel;

/**
 * Handles database interaction as well as the logic for each mission endpoint
 * of the Workflow service.
 */
@Component
public class WorkflowTargetService
{
	@Autowired
	private ApiService apiService;

	/**
	 * Get a list of missions by asset and time window
	 *
	 * @param geometry
	 *            The area to search for targets
	 * @return The list of targets within the area
	 */
	public List<TargetModel> getTargetsByGeometry(
			final String geometry ) {
		if (geometry.isEmpty()) {
			throw new BadRequestException(
					"Geometry cannot be empty");
		}

		return apiService.getTargetsByGeometry(geometry);
	}
}
