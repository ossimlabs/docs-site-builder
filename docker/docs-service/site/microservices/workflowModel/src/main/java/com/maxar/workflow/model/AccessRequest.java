package com.maxar.workflow.model;

import java.util.List;

import com.maxar.access.model.AccessConstraint;

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
public class AccessRequest
{
	/** The list of space asset IDs to use for access generation. */
	@ApiModelProperty(required = true, position = 1, notes = "The list of space asset IDs to use for access generation")
	private List<String> spaceAssetIds;

	/** The start time provided by UI for access generation */
	@ApiModelProperty(required = true, position = 2, notes = "The start time to run access on")
	private String start;

	/** The stop time provided by UI for access generation */
	@ApiModelProperty(required = true, position = 3, notes = "The stop time to run access on")
	private String stop;

	/** The constraints provided by UI for access generation */
	@ApiModelProperty(position = 4, example = "[ { \"name\": \"Graze\", \"minValue\": 20, \"maxValue\": 70 }, "
			+ "{ \"name\": \"Quality\", \"minValue\": 4.0, \"maxValue\": null } ]", notes = "The list of access generation constraints")
	private List<AccessConstraint> accessConstraints;

	/**
	 * Start time buffer (milliseconds) for asset position data generated for CZML.
	 */
	@ApiModelProperty(position = 5, notes = "Start time buffer (milliseconds) for asset position data generated for CZML")
	private Long assetStartTimeBufferMs;

	/**
	 * End time buffer (milliseconds) for asset position data generated for CZML.
	 */
	@ApiModelProperty(position = 6, notes = "End time buffer (milliseconds) for asset position data generated for CZML")
	private Long assetEndTimeBufferMs;
}
