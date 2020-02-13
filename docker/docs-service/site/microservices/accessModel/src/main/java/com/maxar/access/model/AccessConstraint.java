package com.maxar.access.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * The constraint object for constraints provided in the access generation
 * request.
 */
@Data
@ApiModel
public class AccessConstraint
{
	@ApiModelProperty(position = 0, required = true, example = "graze_deg", notes = "Must be the name of a supported contraint, retrievable from the constraints endpoint")
	private String name;

	@ApiModelProperty(position = 1, required = true, example = "20", notes = "Minimum accepted value, \"null\" if there is no minimum.")
	private Double minValue;

	@ApiModelProperty(position = 2, required = true, example = "70", notes = "Maximum accepted value, \"null\" if there is no maximum.")
	private Double maxValue;

}
