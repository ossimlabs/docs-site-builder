package com.maxar.user.model;

import java.util.HashSet;
import java.util.Set;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The wrapper object for a list of session IDs.
 */
@ApiModel
@Data
@NoArgsConstructor
public class UserSessions
{
	/** The user that contains the sessions. */
	@ApiModelProperty(required = true, example = "user", notes = "The user that contains the sessions")
	private String user;

	/** The list of session IDs. */
	@ApiModelProperty(required = true, position = 1, example = "[\"0\"]", notes = "The list of session IDs")
	private Set<String> sessionIds = new HashSet<>();
}
