package com.maxar.workflow.types;

import com.maxar.asset.model.AssetSmearWithBeamsRequest;

public class CesiumAssetSmearRequest extends
		CesiumRequest
{
	public CesiumAssetSmearRequest(
			final String parent,
			final boolean generateParent,
			final boolean displayInTree,
			final String url,
			final AssetSmearWithBeamsRequest body ) {
		this.parent = parent;
		this.generateParent = generateParent;
		this.displayInTree = displayInTree;
		this.url = url;
		this.body = body;
	}
}
