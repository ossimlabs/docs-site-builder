package com.maxar.workflow.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TargetAccessRequest extends
		AccessRequest
{
	/** The list of target IDs to generate accesses for. */
	@ApiModelProperty(required = true, notes = "The list of target IDs to generate accesses for")
	private List<String> targetIds;
}
