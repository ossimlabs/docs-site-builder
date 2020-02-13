package com.maxar.weather.parser;

import java.util.List;

import com.maxar.weather.entity.map.MapGrid;
import com.maxar.weather.entity.weather.WeatherSet;

public abstract class AbstractWeatherParser<GridType extends MapGrid> extends
		DataTypeParser<WeatherSet>
{
	public enum MapGridType {
		WTM,
		RTNEPHQuarterGrid
	}

	private MapGridType mapGridType;

	public MapGridType getMapGridType() {
		return mapGridType;
	}

	public void setMapGridType(
			final MapGridType mapGridType ) {
		this.mapGridType = mapGridType;
	}

	public abstract void setGrids(
			List<GridType> grids );
}
