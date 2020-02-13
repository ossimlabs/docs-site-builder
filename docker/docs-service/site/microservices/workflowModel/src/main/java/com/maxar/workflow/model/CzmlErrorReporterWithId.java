package com.maxar.workflow.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel
@Data
public class CzmlErrorReporterWithId implements
		CzmlErrorReporter
{
	private String id;

	private String czmlError;
}
