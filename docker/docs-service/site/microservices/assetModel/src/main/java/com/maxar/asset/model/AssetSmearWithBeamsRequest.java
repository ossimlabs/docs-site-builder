package com.maxar.asset.model;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@NoArgsConstructor
public class AssetSmearWithBeamsRequest
{
	/** The asset to generate smear and beams for. */
	@ApiModelProperty(required = true, example = "32382", notes = "The asset to generate smear and beams for")
	private String assetId;

	/** The start time for asset smear generation. */
	@ApiModelProperty(required = true, position = 1, example = "2020-01-01T04:30:00.000Z", notes = "The start time for asset smear generation.")
	private String startTimeISO8601;

	/** The stop time for asset smear generation. */
	@ApiModelProperty(required = true, position = 2, example = "2020-01-01T04:43:00.000Z", notes = "The stop time for asset smear generation.")
	private String stopTimeISO8601;

	/** The list of centroid with times defining beams */
	@ApiModelProperty(required = false, position = 3, example = "[ { \"beamCentroidGeo\": \"POINT(111.38368 30.96686 135.0)\", \"startTimeISO8601\": \"2020-01-01T04:42:31.000Z\", \"stopTimeISO8601\": \"2020-01-01T04:42:35.000Z\" }, "
			+ "{ \"beamCentroidGeo\": \"POINT(109.38368 29.96686 135.0)\", \"startTimeISO8601\": \"2020-01-01T04:40:00.000Z\", \"stopTimeISO8601\": \"2020-01-01T04:40:03.000Z\" }, "
			+ "{ \"beamCentroidGeo\": \"POINT(108.38368 20.96686 135.0)\", \"startTimeISO8601\": \"2020-01-01T04:38:00.000Z\", \"stopTimeISO8601\": \"2020-01-01T04:38:04.000Z\" }, "
			+ "{ \"beamCentroidGeo\": \"POINT(109.38368 19.96686 135.0)\", \"startTimeISO8601\": \"2020-01-01T04:35:00.000Z\", \"stopTimeISO8601\": \"2020-01-01T04:35:05.000Z\" }, "
			+ "{ \"beamCentroidGeo\": \"POINT(100.38368 15.96686 135.0)\", \"startTimeISO8601\": \"2020-01-01T04:32:00.000Z\", \"stopTimeISO8601\": \"2020-01-01T04:32:04.000Z\" } ]", notes = "The list of time bound beam/centroids")
	private List<OpBeam> beams;

	/**
	 * The sampling frame rate in seconds for FOR animation. Default is 1 second.
	 */
	@ApiModelProperty(required = false, position = 4, example = "1", notes = "The sampling frame rate in seconds for FOR animation. Default is 1 second.")
	private Integer forFrameIncrementSec;

	/** The czml request includes the smear packet */
	@ApiModelProperty(required = false, position = 5, example = "true", notes = "Will czml format request generate asset smear packet")
	private boolean smearCzmlRequested = true;

	/** The czml request includes the FOR frames packet */
	@ApiModelProperty(required = false, position = 6, example = "true", notes = "Will czml format request generate FOR frames packet")
	private boolean forFramesCzmlRequested = true;

	/** The czml request includes the op beams packet */
	@ApiModelProperty(required = false, position = 7, example = "true", notes = "Will czml format request generate op beams packet")
	private boolean opBeamsCzmlRequested = true;
}
