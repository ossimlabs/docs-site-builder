package com.maxar.spaceobjectcatalog.model;

import java.util.List;

import com.maxar.ephemeris.model.EphemerisModel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@NoArgsConstructor
public class SpaceObject
{
	/** The satellite catalog number (SCN). */
	@ApiModelProperty(required = true, example = "37216", notes = "The satellite catalog number (SCN)")
	private Integer scn;

	/** The list of ephemeris objects for this space object. */
	@ApiModelProperty(position = 1, required = true, notes = "The list of ephemeris objects for this space object")
	private List<EphemerisModel> ephemerides;
}
