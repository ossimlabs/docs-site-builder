package com.maxar.workflow.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request information for access generation.
 */
@ApiModel
@Data
@NoArgsConstructor
public class EphemeridesRequest
{

	/** The list of Asset Names to acquire ephemerides for */
	@ApiModelProperty(required = true, notes = "The list of asset names", example = "[ \"CSM1\"]")
	private List<String> assetNames;

	/** Number of pages requested for ephemerides */
	@ApiModelProperty(required = true, notes = "Number of pages requested for ephemerides")
	private Integer page;

	/** Max number of ephemeris entries requested for the asset */
	@ApiModelProperty(required = true, notes = "Max number of ephemeris entries requested for assets", example = "10")
	private Integer count;

}
