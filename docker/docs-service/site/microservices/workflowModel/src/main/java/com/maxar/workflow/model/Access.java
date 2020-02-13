package com.maxar.workflow.model;

import org.joda.time.DateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.maxar.access.model.AccessValues;

/**
 * A target's access information.
 */
@ApiModel
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Access
{
	/** The identifier of the target to be collected */
	@ApiModelProperty(required = true,
					  notes = "The identifier of the target to be collected")
	private String targetId;

	/** The identifier of the asset that can collect this target. */
	@ApiModelProperty(required = true,
					  position = 1,
					  notes = "The identifier of the asset that can collect this target")
	private String assetId;

	/** The start time of the trimmed/untrimmed access. */
	@ApiModelProperty(required = true,
					  position = 2,
					  notes = "The start time of the untrimmed access")
	private DateTime startTime;

	/** The end time of the trimmed/untrimmed access. */
	@ApiModelProperty(required = true,
					  position = 3,
					  notes = "The end time of the untrimmed access")
	private DateTime endTime;

	/** The reason (if any) for the failure to collect this target. */
	@ApiModelProperty(required = true,
					  position = 4,
					  notes = "The reason (if any) for the failure to collect this target")
	private String failureReason;

	/** The time of closest approach during the access. */
	@ApiModelProperty(required = true,
					  position = 7,
					  notes = "The time of closest approach during the access")
	private DateTime tcaTime;

	/** The country code of the target. */
	@ApiModelProperty(required = true,
					  position = 8,
					  notes = "The country code of the target")
	private String countryCode;

	/** The azimuth at the time of closest approach. */
	@ApiModelProperty(required = true,
					  position = 9,
					  notes = "The azimuth at the time of closest approach")
	private Double azimuth;

	/** The elevation at the time of closest approach. */
	@ApiModelProperty(required = true,
					  position = 10,
					  notes = "The elevation at the time of closest approach")
	private Double elevation;

	/** The quality of the access. */
	@ApiModelProperty(required = true,
					  position = 11,
					  notes = "The quality of the access")
	private Double quality;

	/** The asset sensor mode used to generate the access. */
	@ApiModelProperty(required = true,
					  position = 12,
					  notes = "The asset sensor mode used to generate the access")
	private String sensorMode;

	/** The propagator type used to generate the access. */
	@ApiModelProperty(required = true,
					  position = 13,
					  notes = "The propagator type used to generate the access")
	private String propagatorType;

	@ApiModelProperty(required = true,
					  position = 14,
					  notes = "The CZML representation of the access")
	private List<JsonNode> czml;

	/** The type of the access (trimmed or untrimmed. */
	@ApiModelProperty(required = true,
					  position = 15,
					  notes = "The type of the access")
	private String type;

	/**
	 * The access details value using startTime
	 */
	@ApiModelProperty(required = true,
					  position = 16,
					  notes = "The access details at start time")
	private AccessValues startDetails;

	/**
	 * The access details value using tcaTime
	 */
	@ApiModelProperty(required = true,
					  position = 17,
					  notes = "The access details at tca time")
	private AccessValues tcaDetails;

	/**
	 * The access details value using stopTime
	 */
	@ApiModelProperty(required = true,
					  position = 17,
					  notes = "The access details at stop time")
	private AccessValues stopDetails;

	/** The geoRegion of the target. */
	@ApiModelProperty(required = true,
					  position = 16,
					  notes = "The geoRegion of the target")
	private String geoRegion;

	/** The name of the asset. */
	@ApiModelProperty(required = true,
					  position = 17,
					  notes = "The name of the asset")
	private String assetName;

	/** The sensor type of the asset. */
	@ApiModelProperty(required = true,
					  position = 18,
					  notes = "The sensor type of the asset")
	private String sensorType;

	/** The number of pass for the access. */
	@ApiModelProperty(required = true,
					  position = 19,
					  notes = "The number of pass for the acceses")
	private int pass;

	/** The name of the target. */
	@ApiModelProperty(required = true,
					  position = 20,
					  notes = "The name of the target")
	private String targetName;

	/** The cloud cover percentage. */
	@ApiModelProperty(required = true,
					  position = 21,
					  notes = "The cloud cover percentage over the access")
	private Double cloudCoverPct;

	/** The duration in seconds between the access start and end time. */
	@ApiModelProperty(required = false,
					  position = 22,
					  notes = "The duration in seconds between the access start and end time")
	private Long duration;

	/** The index of the access among the accesses list. */
	@ApiModelProperty(required = false,
					  position = 23,
					  notes = "The index of the access among the accesses list.")
	private Integer index;
}
