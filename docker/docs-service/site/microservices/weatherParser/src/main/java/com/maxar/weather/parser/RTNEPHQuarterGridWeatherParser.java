package com.maxar.weather.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.maxar.weather.entity.map.RTNEPHQuarterGrid;
import com.maxar.weather.entity.weather.Weather;
import com.maxar.weather.entity.weather.WeatherSet;
import com.radiantblue.analytics.core.log.SourceLogger;

public class RTNEPHQuarterGridWeatherParser extends
		AbstractWeatherParser<RTNEPHQuarterGrid>
{
	private static Logger logger = SourceLogger.getLogger(
			new Object() {}.getClass().getEnclosingClass().getName());

	private List<RTNEPHQuarterGrid> grids;

	final private int numWx = 19;
	final private int numHemGrids = 65536;

	class GridWeather
	{
		private int gridNum;
		private char[] wxBuf;
		private boolean north;

		public GridWeather() {}

		public int getGridNum() {
			return gridNum;
		}

		public void setGridNum(
				final int gridNum ) {
			this.gridNum = gridNum;
		}

		public char[] getWxBuf() {
			return wxBuf;
		}

		public void setWxBuf(
				final char[] wxBuf ) {
			this.wxBuf = wxBuf;
		}

		public boolean isNorth() {
			return north;
		}

		public void setNorth(
				final boolean north ) {
			this.north = north;
		}
	}

	public RTNEPHQuarterGridWeatherParser() {}

	public List<RTNEPHQuarterGrid> getGrids() {
		return grids;
	}

	@Override
	public void setGrids(
			final List<RTNEPHQuarterGrid> mapGrids ) {
		grids = mapGrids;
	}

	@Override
	public List<WeatherSet> parseData(
			final InputStream input,
			final int beginIndex,
			final int toIndex )
					throws Exception {

		final BufferedReader br = new BufferedReader(
				new InputStreamReader(
						input));

		// Get forecast atTime
		DateTime atTime = getStartAtTime(
				br);

		// Need to read all the data, and then rearrange into weather sets
		final Map<Integer, GridWeather> northGridWx = new HashMap<Integer, GridWeather>();
		final Map<Integer, GridWeather> southGridWx = new HashMap<Integer, GridWeather>();
		String hem = br.readLine();
		if (hem != null) {
			boolean north = false;
			if (hem == "N") {
				north = true;
			}

			// Read northern hemisphere data
			for (int i = 0; i < numHemGrids; i++) {
				final String gridString = br.readLine();
				final GridWeather g = new GridWeather();
				g.setNorth(
						north);
				final int id = Integer.parseInt(
						gridString.substring(
								0,
								5));
				g.setGridNum(
						id);
				g.setWxBuf(
						gridString.substring(
								5).toCharArray());

				northGridWx.put(
						id,
						g);
			}

			hem = br.readLine();
			north = false;
			if (hem == "N") {
				north = true;
			}

			// Read southern hemisphere data
			for (int i = 0; i < numHemGrids; i++) {
				final String gridString = br.readLine();
				final GridWeather g = new GridWeather();
				g.setNorth(
						north);
				final int id = Integer.parseInt(
						gridString.substring(
								0,
								5));
				g.setGridNum(
						id);
				g.setWxBuf(
						gridString.substring(
								5).toCharArray());

				southGridWx.put(
						id,
						g);
			}
		}
		br.close();

		// Arrange data into weather sets
		final List<WeatherSet> wx = new ArrayList<WeatherSet>();
		for (int i = 0; i < numWx; i++) {
			final WeatherSet wxs = new WeatherSet(
					atTime,
					null);
			wxs.setWeather(
					newWx(
							wxs,
							northGridWx,
							southGridWx,
							i));

			wx.add(
					wxs);
			atTime = atTime.plusHours(
					3);
		}

		return wx;
	}

	public DateTime getStartAtTime(
			final BufferedReader br )
					throws IOException {
		// Read Date Time block in HHMMDDMONYY format
		final String dateString = br.readLine();

		DateTime atTime = null;
		if (dateString.length() == 11) {
			final DateTimeFormatter formatter = DateTimeFormat.forPattern(
					"HHmmddMMMyy");

			logger.debug(
					"atTime is:" + dateString);
			atTime = formatter.parseDateTime(
					dateString);
		}
		else {
			logger.error(
					"Error reading atTime for weather block");
		}
		return atTime;
	}

	public Set<Weather> newWx(
			final WeatherSet weatherSet,
			final Map<Integer, GridWeather> northGridWx,
			final Map<Integer, GridWeather> southGridWx,
			final int idx )
					throws IOException {
		final Set<Weather> weather = new HashSet<Weather>();

		// because our model throws away overlapping grids, need to retrieve
		// weather just for grids we do have
		for (final RTNEPHQuarterGrid theGrid : grids) {
			GridWeather gridWx;
			int cc;
			double cloudCoverPercent;
			if (theGrid.isNorthernHemisphere()) {
				gridWx = northGridWx.get(
						theGrid.getRtnephId());
				cc = gridWx.getWxBuf()[idx] - 'A';
				cloudCoverPercent = cc * 5.0;
			}
			else {
				gridWx = southGridWx.get(
						theGrid.getRtnephId());
				cc = gridWx.getWxBuf()[idx] - 'A';
				cloudCoverPercent = cc * 5.0;
			}
			weather.add(
					new Weather(
							weatherSet,
							theGrid,
							cloudCoverPercent));
		}

		return weather;
	}

	@Override
	public Integer totalToParse(
			final int numberOrWeatherSets ) {
		return numberOrWeatherSets * (2 * numHemGrids);
	}
}
