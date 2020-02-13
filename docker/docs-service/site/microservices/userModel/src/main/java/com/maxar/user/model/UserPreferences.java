package com.maxar.user.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@NoArgsConstructor
public class UserPreferences
{
	public enum CesiumViewType {
		THREE_D(
				"3D"),
		TWO_D(
				"2D"),
		TWO_D_ANGLED(
				"2D Angled");

		private final String text;

		CesiumViewType(
				final String text ) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	public enum CesiumGeoFormat {
		WKT,
		GEO_JSON;
	}

	public static class CesiumColorProperties
	{
		@JsonProperty("propertyName")
		@ApiModelProperty(required = true, example = "czml.target.color", notes = "Cesium UI color property name.")
		private String propertyName;

		@JsonProperty("colorValue")
		@ApiModelProperty(required = true, example = "FF0000", notes = "Color for the cesium UI property.")
		private String colorValue;
	}

	/** The user preferred cesium UI map view type. */
	@ApiModelProperty(required = false, example = "THREE_D", notes = "The user preferred cesium UI map view type.")
	private CesiumViewType viewType;

	/** The list of user preferred AOIs. */
	@ApiModelProperty(required = false, example = "[\"AOI_1\"]", notes = "The list of user preferred AOIs.")
	private List<String> aois;

	/** The user preferred coordinate decimal precision. */
	@ApiModelProperty(required = false, example = "3", notes = "The user preferred coordinate decimal precision.")
	private Integer coordinateDecimalPrecision;

	/** The user preferred geo format displayed in the services UI tables. */
	@ApiModelProperty(required = false, example = "WKT", notes = "The user preferred geo format displayed in the services UI tables.")
	private CesiumGeoFormat geoFormat;

	/** The user preferred cesium UI map color scheme for displayable items. */
	@ApiModelProperty(required = false, notes = "The user preferred cesium UI map color scheme for displayable items.")
	private List<CesiumColorProperties> cesiumColorProperties;
}
