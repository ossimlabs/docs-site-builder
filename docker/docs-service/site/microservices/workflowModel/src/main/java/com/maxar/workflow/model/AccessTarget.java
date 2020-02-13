package com.maxar.workflow.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@NoArgsConstructor
public class AccessTarget
{
	/** The target ID to generate access for. */
	@ApiModelProperty(required = true, notes = "The target ID to generate access for")
	private String targetId;

	/** The geometry (WKT) to generate access for. */
	@ApiModelProperty(required = true, notes = "The geometry (WKT) to generate access for")
	private String geometry;

	/** The name of the target to generate access for. */
	@ApiModelProperty(notes = "The name of the target to generate access for")
	private String targetName;

	/** The country code of the target to generate access for. */
	@ApiModelProperty(notes = "The country code of the target to generate access for")
	private String countryCode;

	/** The geographic region of the target to generate access for. */
	@ApiModelProperty(notes = "The geographic region of the target to generate access for")
	private String geoRegion;
}
