package com.maxar.weather.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.maxar.weather.entity.map.WTM;
import com.maxar.weather.entity.weather.Weather;
import com.maxar.weather.entity.weather.WeatherSet;
import com.radiantblue.analytics.core.log.SourceLogger;

public class AfwaWeatherParser extends
		AbstractWeatherParser<WTM>
{
	private static Logger logger = SourceLogger.getLogger(
			new Object() {}.getClass().getEnclosingClass().getName());

	private boolean currentYear;
	private List<WTM> wtms;

	private static final int assessmentValueSize = 739600; // numWacs * numAtcs
	// * numWtms

	private static final int wxSkipSize = 739600;

	public AfwaWeatherParser() {}

	@Override
	public List<WeatherSet> parseData(
			final InputStream input,
			final int beginIndex,
			final int toIndex )
					throws Exception {

		final List<WeatherSet> wx = new ArrayList<WeatherSet>();

		// On Jan 1, there is 8 bytes of unused data at beginning of file
		// So, if we get an IllegalArgumentException, just try again
		DateTime atTime;
		try {
			atTime = getAtTime(
					input,
					getYear());
		}
		catch (final IllegalArgumentException ex) {
			atTime = getAtTime(
					input,
					getYear());
		}
		while (atTime != null) {
			logger.debug(
					atTime.toString());

			final WeatherSet wxs = new WeatherSet(
					atTime,
					null);

			loadWxData(
					input,
					wxs);

			wx.add(
					wxs);

			// skip unused portion of file
			input.skip(
					wxSkipSize);

			atTime = getAtTime(
					input,
					getYear());
		}
		input.close();

		return wx;
	}

	public DateTime getAtTime(
			final InputStream br,
			final int year )
					throws IOException {
		final byte dateTimeBuf[] = new byte[8];
		// Read Date Time block in MMDDHHMM format
		final int bytesRead = br.read(
				dateTimeBuf,
				0,
				8);

		DateTime atTime = null;
		if (bytesRead == 8) {
			// Make string yyyyMMddHHmm - replace spaces with 0's
			final String dateString = year + new String(
					dateTimeBuf).replaceAll(
							" ",
							"0");
			final DateTimeFormatter formatter = DateTimeFormat.forPattern(
					"yyyyMMddHHmm");

			logger.debug(
					"atTime is:" + dateString);
			atTime = formatter.parseDateTime(
					dateString);
		}
		else if (bytesRead != 0) {
			// Not reporting error if 0...just at end of file
			logger.error(
					"Error reading atTime for weather block");
		}
		return atTime;
	}

	public void loadWxData(
			final InputStream br,
			final WeatherSet wxs )
					throws Exception {

		final byte cbuf[] = IOUtils.toByteArray(
				br,
				assessmentValueSize);

		if (cbuf.length == assessmentValueSize) {
			wxs.setWeather(
					newWx(
							wxs,
							cbuf));
		}
		else {
			logger.error(
					"BufferedReader error Data for Weather Data");
		}
	}

	public Set<Weather> newWx(
			final WeatherSet weatherSet,
			final byte cbuf[] )
					throws IOException {
		final Set<Weather> weather = new HashSet<Weather>();

		int i = 0; // index
		double cloudCoverPercent;
		for (final WTM curWTM : wtms) {
			cloudCoverPercent = ((char) cbuf[i++]) * 5.0;
			weather.add(
					new Weather(
							weatherSet,
							curWTM,
							cloudCoverPercent));
		}

		return weather;
	}

	public boolean isCurrentYear() {
		return currentYear;
	}

	public void setCurrentYear(
			final boolean currentYear ) {
		this.currentYear = currentYear;
	}

	@Override
	public void setGrids(
			final List<WTM> wtms ) {
		this.wtms = wtms;
	}

	public int getYear() {
		int year;
		if (currentYear) {
			year = DateTime.now().getYear();
		}
		else {
			year = Integer.parseInt(
					getMetaData().substring(
							9,
							13));
		}
		return year;
	}

	@Override
	public Integer totalToParse(
			final int numberOrWeatherSets ) {
		return numberOrWeatherSets * assessmentValueSize;
	}
}
