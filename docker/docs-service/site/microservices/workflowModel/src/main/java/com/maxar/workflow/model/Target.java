package com.maxar.workflow.model;

import org.joda.time.DateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A container for target information.
 */
@ApiModel
@Data
@NoArgsConstructor
public class Target implements
		CzmlErrorReporter
{
	/** The identifier of the target. */
	@ApiModelProperty(required = true, notes = "The identifier of the target")
	private String targetId;

	/** The name of the target. */
	@ApiModelProperty(position = 1, required = true, notes = "The name of the target")
	private String targetName;

	/** The country code of the target. */
	@ApiModelProperty(position = 2, required = true, notes = "The country code of the target")
	private String countryCode;

	/** The region code of the target. */
	@ApiModelProperty(position = 3, required = true, notes = "The region code of the target")
	private String geoRegion;

	/** The well-known text (WKT) of the target geometry. */
	@ApiModelProperty(position = 4, required = true, notes = "The well-known text (WKT) of the target geometry")
	private String geometryWkt;

	/** The well-known text (WKT) of the target centroid. */
	@ApiModelProperty(position = 5, required = true, notes = "The well-known text (WKT) of the target centroid")
	private String centroidWkt;

	/** The indicator for if this is an estimated target (Not in database). */
	@ApiModelProperty(position = 6, required = true, notes = "The indicator for if this is an estimated target (Not in database).")
	private boolean estimated;

	/** The start time of the event associated with this target instance. */
	@ApiModelProperty(position = 7, required = true, notes = "The start time of the event associated with this target instance.")
	private DateTime eventStart;

	/** The stop time of the event associated with this target instance. */
	@ApiModelProperty(position = 8, required = true, notes = "The stop time of the event associated with this target instance.")
	private DateTime eventStop;

	private String czmlError;
}
