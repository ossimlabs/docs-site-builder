package com.maxar.workflow.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@NoArgsConstructor
public class AccessDetailsRequest
{
	@ApiModelProperty(required = true, position = 1, notes = "Asset ID", example = "RS02")
	private String assetId;
	@ApiModelProperty(required = true, position = 2, notes = "Sensor Mode Name", example = "RS02_Framing_Mode")
	private String sensorModeName;
	@ApiModelProperty(required = true, position = 3, notes = "Propagator Type", example = "SGP4")
	private String propagatorType;
	@ApiModelProperty(required = true, position = 4, notes = "Geometry String using well known text (WKT)", example = "POINT(-77.38368 38.96686 135.0)")
	private String geometry;
	@ApiModelProperty(required = true, position = 5, notes = "ISO8601 Formatted Date String", example = "2020-01-01T21:40:17.742Z")
	private String atTime;
}
