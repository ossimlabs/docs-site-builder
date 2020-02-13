package com.maxar.weather.model.map;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxar.common.utils.GeometryDeserializer;
import com.maxar.common.utils.GeometrySerializer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class MapGridModel
{
	private String id;
	
	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	private Geometry geometry;
}
