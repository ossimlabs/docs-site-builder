package com.maxar.init.database.loaders.ephemeris.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.maxar.ephemeris.entity.TLE;
import com.maxar.ephemeris.entity.utils.TLEUtils;
import com.radiantblue.analytics.core.log.SourceLogger;

public class TLEImporter
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	public static List<TLE> parseTLEsFromFile(
			final List<Integer> scns,
			final String file ) {
		final char defaultClassFiller = 'U';
		final Map<Integer, Integer> defaultAliasesEmpty = new HashMap<Integer, Integer>();
		final List<TLE> tles = parseTLEsFromFile(	scns,
													file,
													defaultClassFiller,
													defaultAliasesEmpty);
		return tles;
	}

	public static List<TLE> parseTLEsFromFile(
			final List<Integer> scns,
			final String file,
			final char classFiller,
			final Map<Integer, Integer> scnAliases ) {
		List<TLE> returnTLEs = null;

		final File tleFile = new File(
				file);

		if (tleFile.exists()) {
			try {
				final FileReader fr = new FileReader(
						tleFile.getAbsoluteFile());
				final BufferedReader br = new BufferedReader(
						fr);
				returnTLEs = parseTLEsFromReader(	scns,
													br,
													classFiller,
													scnAliases);
				br.close();
			}
			catch (final FileNotFoundException e) {

				logger.error(	"Tle file not found: " + file,
								e);
			}
			catch (final IOException e) {
				logger.error(	"IO error while parsing file: " + file,
								e);
			}
		}
		else {
			logger.error("TLE file does not exist: " + file);
		}

		return returnTLEs;
	}

	public static List<TLE> parseTLEsFromReader(
			final List<Integer> scns,
			final BufferedReader br,
			final char classFiller,
			final Map<Integer, Integer> scnAliases )
			throws IOException {
		final List<TLE> returnTLEs = new ArrayList<TLE>();

		String header = null;
		String tleLineOne = null;
		String tleLineTwo = null;
		int headerLineCount = 0;

		String line = br.readLine();
		while (line != null) {
			line = line.trim();

			// check for comment
			if (line.startsWith("#")) {
				line = br.readLine();
				continue;
			}

			if ((tleLineOne == null) && (tleLineTwo == null)) {
				if (TLEUtils.isValidLineOneSimpleParseCheck(line)) {
					tleLineOne = line;
				}
				else {
					header = line;
					headerLineCount++;
				}
			}
			else if ((tleLineOne != null) && (tleLineTwo == null)) {
				if (TLEUtils.isValidLineTwoSimpleParseCheck(line)) {
					tleLineTwo = line;
				}
			}

			if ((tleLineOne != null) && (tleLineTwo != null)) {
				final String description;
				if ((header != null) && (headerLineCount == 1)) {
					description = header;
				}
				else {
					description = "";
				}

				final TLE tleTemp = TLEUtils.parseLinesTLE(	tleLineOne,
															tleLineTwo,
															classFiller,
															scnAliases);

				if (tleTemp != null) {
					final String tleLineOneUpdate = tleTemp.getTleLineOne();
					final int scn = TLEUtils.getSCNFromTleLineOne(tleLineOneUpdate);
					if (scns.contains(scn)) {
						tleTemp.setScn(scn);
						tleTemp.setDescription(description);
						// copy constructor sets epoch values
						final TLE tle = new TLE(
								tleTemp);
						returnTLEs.add(tle);
					}
				}

				header = null;
				tleLineOne = null;
				tleLineTwo = null;
				headerLineCount = 0;
			}

			line = br.readLine();
		}
		return returnTLEs;
	}

}
