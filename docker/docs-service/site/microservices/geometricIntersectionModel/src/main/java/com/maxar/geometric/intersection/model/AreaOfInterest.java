package com.maxar.geometric.intersection.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An area of interest to be ingested into the Geometry Intersection service.
 */
@ApiModel
@Data
@NoArgsConstructor
public class AreaOfInterest
{
	/** The unique identifier for this area of interest. */
	@ApiModelProperty(required = true, example = "test-aoi-01", notes = "The unique identifier")
	private String id;

	/** The WKT geometry of the area of interest. */
	@ApiModelProperty(required = true, position = 1, example = "POLYGON ((29.70703125 24.407137917727667, "
			+ "31.596679687499996 24.407137917727667, 31.596679687499996 26.293415004265796, "
			+ "29.70703125 26.293415004265796, "
			+ "29.70703125 24.407137917727667))", notes = "The polygon geometry of the area of interest (in well-known text, WKT; "
					+ "points should be longitude (degrees), latitude (degrees), (optional) altitude (meters))")
	private String geometryWkt;
}
