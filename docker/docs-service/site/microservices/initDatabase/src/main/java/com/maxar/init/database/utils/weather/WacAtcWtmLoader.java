package com.maxar.init.database.utils.weather;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.maxar.common.utils.WacUtils;
import com.maxar.weather.entity.map.ATC;
import com.maxar.weather.entity.map.WAC;
import com.maxar.weather.entity.map.WTM;
import com.radiantblue.analytics.core.log.SourceLogger;

public class WacAtcWtmLoader
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	private static int NUM_WACS = 1849; // AFWA for WAC 2-1850
	private static int NUM_ATCS = 25;
	private static int NUM_WTMS = 16;

	public WacAtcWtmLoader() {}

	@SuppressWarnings({
		"rawtypes",
		"unchecked"
	})
	public static List<WAC> createAFWAWacs(
			final String wacFile ) {
		BasicConfigurator.configure();

		final List<WAC> wacs = new ArrayList<WAC>();

		WacUtils.setGridFile(wacFile);

		// numWacs+1 so WACIds are 2-1850 which contain weather data
		for (int wacNum = 2; wacNum <= (NUM_WACS + 1); wacNum++) {
			final WAC wac = new WAC(
					wacNum);
			wac.setGeometry(WacUtils.getInstance()
					.buildPolyFromId(WacUtils.getInstance().new WACData(
							wacNum,
							0,
							0))
					.splitOnDateLine()
					.jtsGeometry_deg());

			final Set atcList = new HashSet<ATC>();
			wac.setAtcs(atcList);

			for (int atcNum = 1; atcNum <= NUM_ATCS; atcNum++) {
				final ATC atc = new ATC(
						wac,
						atcNum);
				atc.setGeometry(WacUtils.getInstance()
						.buildPolyFromId(WacUtils.getInstance().new WACData(
								wacNum,
								atcNum,
								0))
						.splitOnDateLine()
						.jtsGeometry_deg());

				atcList.add(atc);
				final Set wtmList = new HashSet<WTM>();
				atc.setWtms(wtmList);

				for (int wtmNum = 1; wtmNum <= NUM_WTMS; wtmNum++) {
					final WTM wtm = new WTM(
							atc,
							wtmNum);
					wtm.setGeometry(WacUtils.getInstance()
							.buildPolyFromId(WacUtils.getInstance().new WACData(
									wacNum,
									atcNum,
									wtmNum))
							.splitOnDateLine()
							.jtsGeometry_deg());

					wtmList.add(wtm);
				}

				atc.setWtms(wtmList);
			}

			wac.setAtcs(atcList);
			wacs.add(wac);
		}

		// debug
		logger.info("Size of wacs: " + wacs.size());
		return wacs;

	}
}
