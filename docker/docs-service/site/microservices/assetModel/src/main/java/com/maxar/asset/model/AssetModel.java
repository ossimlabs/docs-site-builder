package com.maxar.asset.model;

import lombok.Getter;
import lombok.Setter;

/**
 * The wrapper object for an asset model's XML document.
 *
 * This class is subclassed to provide extra documentation for the API by
 * specializing for each type of asset.
 */
public class AssetModel
{
	/** The asset model XML document. */
	@Getter
	@Setter
	private String modelXml;
}
