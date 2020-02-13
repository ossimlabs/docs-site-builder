package com.maxar.workflow.types;

import com.maxar.access.model.AccessGenerationRequest;

/**
 * Stores the URL that the cesium server needs to send the czml request to the
 * appropriate service Store the body with data needed by certain services
 * (access service) to produce the correct czml for the cesium-server
 */
public class CesiumSpaceAccessRequest extends
		CesiumRequest
{
	public CesiumSpaceAccessRequest(
			final String parent,
			final boolean generateParent,
			final boolean displayInTree,
			final String url,
			final AccessGenerationRequest body ) {
		this.parent = parent;
		this.generateParent = generateParent;
		this.displayInTree = displayInTree;
		this.url = url;
		this.body = body;
	}
}
