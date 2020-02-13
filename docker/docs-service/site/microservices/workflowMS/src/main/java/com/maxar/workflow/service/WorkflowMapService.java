package com.maxar.workflow.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxar.workflow.exception.CzmlGenerationException;
import com.maxar.workflow.model.CzmlErrorReporterWithId;

/**
 * Handles interaction of controls of the cesium map server from the services
 * UI.
 */
@Component
public class WorkflowMapService
{
	@Autowired
	private ApiService apiService;

	/**
	 * @return Any czml errors if they occurred
	 * @param session
	 *            session to clear
	 */
	public CzmlErrorReporterWithId deleteAllCzml(
			final String session ) {
		try {
			apiService.deleteCzml(session);
		}
		catch (final CzmlGenerationException e) {
			final CzmlErrorReporterWithId czmlErrorReporterWithId = new CzmlErrorReporterWithId();
			czmlErrorReporterWithId.setId(session);
			czmlErrorReporterWithId.setCzmlError(e.getMessage());

			return czmlErrorReporterWithId;
		}

		return null;
	}


	/**
	 * @return Any czml errors if they occurred
	 * @param session
	 *            session to clear
	 */
	public CzmlErrorReporterWithId displayCzml(
			final String session,
			final List<JsonNode> czml ) {
		try {
			apiService.displayCzml(	session,
									   czml);
		}
		catch (final CzmlGenerationException e) {
			final CzmlErrorReporterWithId czmlErrorReporterWithId = new CzmlErrorReporterWithId();
			czmlErrorReporterWithId.setId(session);
			czmlErrorReporterWithId.setCzmlError(e.getMessage());

			return czmlErrorReporterWithId;
		}

		return null;
	}
}