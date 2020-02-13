package com.maxar.ephemeris.dm.dataingest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.maxar.ephemeris.entity.TLE;
import com.maxar.ephemeris.entity.utils.TLEUtils;
import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.manager.dataingest.DataTypeParser;

public class SampleTleParser extends
		DataTypeParser<TLE>
{

	@Override
	public List<TLE> parseData(
			final InputStream input,
			final int beginIndex,
			final int toIndex )
			throws Exception {
		final BufferedReader reader = new BufferedReader(
				new InputStreamReader(
						input));

		final List<TLE> tles = new ArrayList<>();

		String header = null;
		String tleLineOne = null;
		String tleLineTwo = null;
		int headerLineCount = 0;

		final char classFiller = 'U';

		String line = reader.readLine();
		while (line != null) {
			line = line.trim();

			// check for comment
			if (line.startsWith("#")) {
				line = reader.readLine();
				continue;
			}

			if (tleLineOne == null) {
				if (TLEUtils.isValidLineOneSimpleParseCheck(line)) {
					tleLineOne = line;
				}
				else {
					header = line;
					headerLineCount++;
				}
			}
			else if (TLEUtils.isValidLineTwoSimpleParseCheck(line)) {
				tleLineTwo = line;
			}

			if ((tleLineOne != null) && (tleLineTwo != null)) {
				final Optional<TLE> tle = populateTLE(	tleLineOne,
														tleLineTwo,
														header,
														headerLineCount,
														classFiller);

				tle.ifPresent(tles::add);

				header = null;
				tleLineOne = null;
				tleLineTwo = null;
				headerLineCount = 0;
			}

			line = reader.readLine();
		}
		return tles;
	}

	private Optional<TLE> populateTLE(
			final String tleLineOne,
			final String tleLineTwo,
			final String header,
			final int headerLineCount,
			final char classFiller ) {
		final String description;
		if ((header != null) && (headerLineCount == 1)) {
			description = header;
		}
		else {
			description = "";
		}

		final TLE tle = TLEUtils.parseLinesTLE(	tleLineOne,
												tleLineTwo,
												classFiller,
												null);

		if (tle != null) {
			final String tleLineOneUpdate = tle.getTleLineOne();
			final int scn = TLEUtils.getSCNFromTleLineOne(tleLineOneUpdate);

			tle.setScn(scn);
			tle.setDescription(description);
			tle.setType(EphemerisType.TLE);
		}
		return Optional.ofNullable(tle);
	}

}
