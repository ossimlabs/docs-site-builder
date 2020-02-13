package com.maxar.asset.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * The wrapper object for extra data for an asset model.
 */
@ApiModel
public class ExtraData
{
	/** The type of the extra data. */
	@ApiModelProperty(required = true,
					  example = "Text",
					  notes = "The type of extra data")
	@Getter
	@Setter
	private String type;

	/** The extra data. */
	@ApiModelProperty(position = 1,
					  required = true,
					  example = "This is extra data for an asset",
					  notes = "The extra data")
	@Getter
	@Setter
	private String data;
}
