package com.maxar.asset.model;

import com.maxar.asset.example.AssetModelExamples;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Subclass of AssetModel to specialize the ApiModel documentation for space
 * assets.
 */
@ApiModel
public class SpaceAssetModel extends
		AssetModel
{
	@ApiModelProperty(required = true,
					  example = AssetModelExamples.SPACE_WV01_XML,
					  notes = "The space asset model as an XML document")
	public String getModelXml() {
		return super.getModelXml();
	}
}
