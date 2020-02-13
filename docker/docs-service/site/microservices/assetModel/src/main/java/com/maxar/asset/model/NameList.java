package com.maxar.asset.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * The wrapper object for a list of names.
 */
@ApiModel
public class NameList
{
	/** The list of names. */
	@ApiModelProperty(required = true,
					  example = "[\"name\"]",
					  notes = "The list of names")
	@Getter
	@Setter
	private List<String> names;
}
