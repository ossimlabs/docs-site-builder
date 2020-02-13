package com.maxar.workflow.types;

import com.maxar.opgen.model.OpSpaceRequest;

public class CesiumSpaceOpsRequest extends
		CesiumRequest
{
	public CesiumSpaceOpsRequest(
			final String parent,
			final boolean generateParent,
			final boolean displayInTree,
			final String url,
			final OpSpaceRequest body ) {
		this.parent = parent;
		this.generateParent = generateParent;
		this.displayInTree = displayInTree;
		this.url = url;
		this.body = body;
	}
}
