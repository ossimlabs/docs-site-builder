package com.maxar.asset.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * The wrapper object for a list of model IDs.
 */
@ApiModel
public class IdList
{
	/** The list of model IDs. */
	@ApiModelProperty(required = true,
					  example = "[\"32060\"]",
					  notes = "The list of model IDs")
	@Getter
	@Setter
	private List<String> ids;
}
