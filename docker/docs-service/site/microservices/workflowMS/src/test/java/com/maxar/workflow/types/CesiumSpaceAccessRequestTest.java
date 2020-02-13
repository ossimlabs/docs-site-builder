package com.maxar.workflow.types;

import org.junit.Assert;
import org.junit.Test;

import com.maxar.access.model.AccessGenerationRequest;

public class CesiumSpaceAccessRequestTest
{
	private static final String EXAMPLE_PARENT = "parent";

	private static final String EXAMPLE_URL = "http://localhost:8080/";

	@Test
	public void testCesiumSpaceAccessRequest() {
		final AccessGenerationRequest accessGenerationRequest = new AccessGenerationRequest();

		final CesiumSpaceAccessRequest cesiumSpaceAccessRequest = new CesiumSpaceAccessRequest(
				EXAMPLE_PARENT,
				false,
				false,
				EXAMPLE_URL,
				accessGenerationRequest);

		Assert.assertNotNull(cesiumSpaceAccessRequest);
		Assert.assertEquals(EXAMPLE_PARENT,
							cesiumSpaceAccessRequest.getParent());
		Assert.assertFalse(cesiumSpaceAccessRequest.isGenerateParent());
		Assert.assertFalse(cesiumSpaceAccessRequest.isDisplayInTree());
		Assert.assertEquals(EXAMPLE_URL,
							cesiumSpaceAccessRequest.getUrl());
	}
}
