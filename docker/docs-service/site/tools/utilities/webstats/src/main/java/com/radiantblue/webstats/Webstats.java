package com.radiantblue.webstats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Webstats
{

	public Webstats() {}

	/*
	 * will read each localhost_access_log file in the provided 'inDirectory' and
	 * write results to output file, calculate statistics calls per day, by IP, by asset, 
	 * by REST call, and cumulative calls
	 * 
	 */

	public static void calcStats(
			final String inDirectory,
			final String outFileName )
					throws IOException {
		final List<String> justLocalHostAccessFilesList = new ArrayList<String>();

		final HashMap<String, Integer> totalCallsByDay = new HashMap<String, Integer>();
		final HashMap<String, Integer> totalCallsByIP = new HashMap<String, Integer>();
		final HashMap<String, Integer> totalCallsByAsset = new HashMap<String, Integer>();
		final HashMap<String, Integer> totalCallsByType = new HashMap<String, Integer>();

		Integer totalCalls = 0;

		final File[] files = new File(
				inDirectory).listFiles();

		for (final File file : files) {
			if (file.isFile() && file.getName().contains(
					"localhost_access_log")) {
				justLocalHostAccessFilesList.add(
						file.getAbsolutePath());
			}
		}

		// Open files and do stats
		for (final String curFile : justLocalHostAccessFilesList) {
			// 
			System.out.println(
					curFile);

			final InputStream is = new FileInputStream(
					curFile);
			final InputStreamReader isr = new InputStreamReader(
					is);
			final BufferedReader br = new BufferedReader(
					isr);
			String curLine = "";
			while ((curLine = br.readLine()) != null) {
				// should be in each line of file
				final String curIP = curLine.substring(
						0,
						curLine.indexOf(
								' '));
				final String curDate = curFile.substring(
						curFile.length() - 14,
						curFile.length() - 4);

				final int curDatesCount = totalCallsByDay.containsKey(
						curDate) ? totalCallsByDay.get(
								curDate) : 0;
				totalCallsByDay.put(
						curDate,
						curDatesCount + 1);

				final int curIPCount = totalCallsByIP.containsKey(
						curIP) ? totalCallsByIP.get(
								curIP) : 0;
				totalCallsByIP.put(
						curIP,
						curIPCount + 1);

				totalCalls++;

				// optional values
				final ArrayList<String> assets = getAssetsFromLine(
						curLine);
				if (assets != null) {
					for (final String curAsset : assets) {
						final int curAssetCount = totalCallsByAsset.containsKey(
								curAsset)
										? totalCallsByAsset.get(
												curAsset)
										: 0;
						totalCallsByAsset.put(
								curAsset,
								curAssetCount + 1);
					}
				}

				final String curType = StringUtils.substringBetween(
						curLine,
						"rest/",
						"?");

				if (curType != null) {
					final int curTypeCount = totalCallsByType.containsKey(
							curType) ? totalCallsByType.get(
									curType) : 0;
					totalCallsByType.put(
							curType,
							curTypeCount + 1);
				}

			}

			is.close();
		}

		// sort keys
		final Object[] dateKeys = totalCallsByDay.keySet().toArray();
		Arrays.sort(
				dateKeys);

		final Object[] ipKeys = totalCallsByIP.keySet().toArray();
		Arrays.sort(
				ipKeys);

		final Object[] assetKeys = totalCallsByAsset.keySet().toArray();
		Arrays.sort(
				assetKeys);

		final Object[] typeKeys = totalCallsByType.keySet().toArray();
		Arrays.sort(
				typeKeys);

		// write out stats
		final PrintWriter writer = new PrintWriter(
				outFileName);

		writer.println(
				"Total Calls: " + totalCalls);
		writer.println();

		writer.println(
				"Date" + '\t' + "Count");

		for (final Object key : dateKeys) {

			writer.println(
					key.toString() + '\t' + totalCallsByDay.get(
							key));
		}
		writer.println();

		writer.println(
				"IP\tCount");

		for (final Object key : ipKeys) {

			writer.println(
					key.toString() + '\t' + totalCallsByIP.get(
							key));
		}
		writer.println();

		writer.println(
				"Asset\tCount");

		for (final Object key : assetKeys) {

			writer.println(
					key.toString() + '\t' + totalCallsByAsset.get(
							key));
		}
		writer.println();

		writer.println(
				"Call Type\tCount");

		for (final Object key : typeKeys) {

			writer.println(
					key.toString() + '\t' + totalCallsByType.get(
							key));
		}

		writer.close();

	}

	private static ArrayList<String> getAssetsFromLine(
			final String curLine ) {
		final ArrayList<String> assets = new ArrayList<String>();

		final int numAssets = StringUtils.countMatches(
				curLine,
				"&asset=");

		if (numAssets > 0) {
			final String[] foundAssets = StringUtils.substringsBetween(
					curLine,
					"&asset=",
					"&");
			if (foundAssets != null) {
				for (final String curAsset : foundAssets) {
					assets.add(
							curAsset.trim());
				}
				if (foundAssets.length == numAssets) {
					return assets;
				}
			}
			else {
				// asset last item in request
				final String[] foundLastAsset = StringUtils.substringsBetween(
						curLine,
						"&asset=",
						"HTTP");
				if (foundLastAsset != null) {
					for (final String curAsset : foundLastAsset) {
						assets.add(
								curAsset.trim());
					}
				}
			}
		}

		return assets;
	}

	public static void main(
			final String[] args ) {

		try {
			//Webstats.calcStats(
			//		"C:\\Users\\jweller\\Desktop\\ASAPStats",
			//		"C:\\Users\\jweller\\Desktop\\ASAPStats\\stats.txt");
			Webstats.calcStats(args[0], args[1]);
		}
		catch (final IOException e) {

			e.printStackTrace();
		}

	}

}
