package com.maxar.workflow.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public interface CzmlErrorReporter
{
	/** The error that prevented CZML from being generated. */
	@ApiModelProperty(notes = "The error that prevented CZML from being generated ")
	String getCzmlError();

	void setCzmlError(final String czmlError);
}
