package com.maxar.init.database.loaders.target;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.maxar.common.utils.WacUtils;
import com.maxar.common.utils.WacUtils.WACData;
import com.maxar.target.entity.BasTarget;
import com.maxar.target.entity.BasWtm;
import com.maxar.target.entity.DsaTarget;
import com.maxar.target.entity.LocSegmentTarget;
import com.maxar.target.entity.LocTarget;
import com.maxar.target.entity.PointTarget;
import com.maxar.target.entity.Target;
import com.maxar.target.model.TerrainType;
import com.maxar.target.repository.TargetRepository;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.geometry.GeodeticPolygon;

@EnableAutoConfiguration
@ComponentScan(basePackages = {
	"com.maxar.target.controller",
	"com.maxar.target.configuration"
})
@EnableJpaRepositories("com.maxar.target.repository")
@EntityScan("com.maxar.target.entity")
public class InitTargets implements
		CommandLineRunner
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	// use own randomGenerator to ensure repeatable results
	private static Random randomGenerator = new Random(
			8675309);

	static final int MAX_TARGETS = 0;
	static final double DSA_PERCENT = 0.1;
	static final double LOC_PERCENT = 0.05;
	static final double BAS_PERCENT = 0.05;

	@Autowired
	private TargetRepository targetRepository;

	private String targetFile;
	private String wacFile;

	static private String ApplicationName = "inittargets";
	static private String PropertiesFile = ApplicationName + ".properties";

	private void loadProperties() {
		final Properties props = new Properties();

		final InputStream is = Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(PropertiesFile);

		if (is == null) {
			logger.error("Unable to load Generic Manager properties: " + PropertiesFile);
			System.exit(0);
		}

		logger.info("Loading properties: " + PropertiesFile);
		try {
			props.load(is);
		}
		catch (final IOException e) {

			logger.error(	"Failed to load : " + PropertiesFile + "\n",
							e);
		}

		String p;
		if ((p = props.getProperty("initDatabase.targetFile")) != null) {
			targetFile = p.trim();
		}
		if ((p = props.getProperty("initTargets.wacGridFile")) != null) {
			wacFile = p.trim();
		}
	}

	/**
	 * Initialize sample targets from file
	 *
	 * @param targetFile
	 *            {@link String} filename
	 */
	public void initTargets(
			final String targetFile ) {
		BasicConfigurator.configure();

		WacUtils.setGridFile(wacFile);

		logger.info("### Importing Target file: " + targetFile);

		final List<Target> targets = new ArrayList<>();

		logger.info("Loading target locations...");
		try (InputStream input = openInputStream(targetFile)) {
			int lines = 0;
			int locs = 0;
			int dsas = 0;
			int numBas = 0;
			int points = 0;

			final BufferedReader br = new BufferedReader(
					new InputStreamReader(
							input));

			while (br.ready()) {

				final String line = br.readLine();
				lines++;

				// skip headers
				if (lines <= 1) {
					// RESTART LOAD
					continue;
				}

				final String tokens[] = line.split("\t");

				final String targetName = tokens[0].replaceAll(	"\"",
																""); // remove
																		// quotes
				final String cc = tokens[2].replaceAll(	"\"",
														""); // remove quotes;

				// TODO: Add GEOREGION
				final String gr = "ZZ";

				final double lat = Double.parseDouble(tokens[3]);
				final double lon = Double.parseDouble(tokens[4]);
				// double length = Double.parseDouble(tokens[11]); // KM
				// double width = Double.parseDouble(tokens[12]); // KM
				final double alt = Double.parseDouble(tokens[17]); // KM

				Target t = null;

				// determine target type
				final double random = randomGenerator.nextDouble();

				if (random < DSA_PERCENT) {
					dsas++;

					// random size (0.1 - 1.0 deg)

					final GeodeticPoint center = GeodeticPoint.fromLatLon(	Angle.fromDegrees(lat),
																			Angle.fromDegrees(lon));

					final double r = randomGenerator.nextDouble();

					final Length oneDegree = Length.fromKilometers(110)
							.times(Math.cos(center.latitude()
									.radians()));

					List<GeodeticPoint> poly;

					// Commercial EO area options...
					if (r < .1) {
						poly = makePoly(center,
										Length.fromKilometers(110),
										Length.fromKilometers(15));
					}
					else if (r < .2) {
						poly = makePoly(center,
										Length.fromKilometers(110),
										Length.fromKilometers(30));
					}
					else if (r < .3) {
						poly = makePoly(center,
										Length.fromKilometers(110),
										Length.fromKilometers(45));
					}
					else if (r < .4) {
						poly = makePoly(center,
										Length.fromKilometers(15),
										oneDegree);
					}
					else if (r < .5) {
						poly = makePoly(center,
										Length.fromKilometers(30),
										oneDegree);
					}
					else if (r < .6) {
						poly = makePoly(center,
										Length.fromKilometers(45),
										oneDegree);
					}
					else if (r < .7) {
						poly = makePoly(center,
										Length.fromKilometers(30),
										Length.fromKilometers(15));
					}
					else if (r < .8) {
						poly = makePoly(center,
										Length.fromKilometers(45),
										Length.fromKilometers(15));
					}
					else if (r < .9) {
						poly = makePoly(center,
										Length.fromKilometers(15),
										Length.fromKilometers(30));
					}
					else {
						poly = makePoly(center,
										Length.fromKilometers(15),
										Length.fromKilometers(45));
					}

					final Geometry geom = GeodeticPolygon.create(poly)
							.splitOnDateLine()
							.jtsGeometry_deg();

					final DsaTarget dsa = new DsaTarget(
							String.format(	"D%05d",
											dsas),
							"DSA " + dsas,
							geom);
					dsa.setDescription(targetName);
					dsa.setTerrainType(TerrainType.LAND);

					t = dsa;
				}
				else if (random < (DSA_PERCENT + LOC_PERCENT)) {
					locs++;

					final Set<LocSegmentTarget> locSegs = new HashSet<>();

					// create random LOC
					GeodeticPoint last = GeodeticPoint.fromLatLon(	Angle.fromDegrees(lat),
																	Angle.fromDegrees(lon));

					// random 2-10 segments
					final int numPoints = (int) ((randomGenerator.nextDouble() * 8) + 2);

					final String locId = String.format(	"L%05d",
														locs);

					final LocTarget loc = new LocTarget(
							locId,
							"LOC " + locs,
							locSegs);
					loc.setDescription(targetName);

					int az = (int) (randomGenerator.nextDouble() * 360);

					int j = 0;
					while (j < numPoints) {

						// move random direction and length (limit az to 45
						// degrees from last one)
						az += (int) ((randomGenerator.nextDouble() * 90) - 45);

						final int distKm = (int) ((randomGenerator.nextDouble() * 9) + 1);

						final GeodeticPoint next = last.move(	Angle.fromDegrees(az),
																Length.fromKilometers(distKm));

						// create LocSegment targets for each segment
						final String id = locId + String.format("%02d",
																j);
						final LocSegmentTarget ls = new LocSegmentTarget(
								loc,
								id,
								"LOC SEGMENT " + id,
								j,
								last,
								next);

						locSegs.add(ls);

						last = next;

						j++;
					}

					// Must update geometry for LOC since had to be created
					// BEFORE segments created.
					loc.setSegments(locSegs);
					loc.updateGeometry();

					t = loc;
				}
				else if (random < (DSA_PERCENT + LOC_PERCENT + BAS_PERCENT)) {
					numBas++;

					// create random BAS
					final GeodeticPoint pt = GeodeticPoint.fromLatLon(	Angle.fromDegrees(lat),
																		Angle.fromDegrees(lon));

					// Use the lat/lon to determine WAC/ATC then randomly select
					final WACData wacData = WacUtils.getInstance()
							.latLonToWACData(pt);

					if ((wacData.wac < 1) || (wacData.wac > 1851) || (wacData.atc < 1) || (wacData.atc > 25)
							|| (wacData.wtm < 1) || (wacData.wtm > 16)) {
						logger.error("Bad WAC/ATC/WTM: " + wacData.wac + "/" + wacData.atc + "/" + wacData.wtm
								+ " Target:" + targetName + " lat/lon:" + lat + "," + lon);
						// TODO: For now just skip this target
						continue;
					}

					final Set<BasWtm> wtms = new HashSet<>();

					// randomly select 1-16 wtms to attach to this BAS
					final int numWtms = (int) ((randomGenerator.nextDouble() * 15) + 1);

					final String basId = String.format(	"B%05d",
														numBas);

					final BasTarget bas = new BasTarget(
							basId,
							"BAS " + numBas,
							wtms);
					bas.setDescription(targetName);

					int j = 0;
					while (j < numWtms) {
						j++;

						final WACData curWacData = WacUtils.getInstance().new WACData(
								wacData.wac,
								wacData.atc,
								j);

						// create BasWtm targets for each BAS WTM
						final String mapGridId = String.format(	"%04d%02d%02d",
																curWacData.wac,
																curWacData.atc,
																curWacData.wtm);
						final BasWtm wtm = new BasWtm(
								bas,
								mapGridId);

						wtms.add(wtm);
					}

					// Must update geometry for BAS since had to be created
					// BEFORE BAS WTMs created.
					bas.setWtms(wtms);
					bas.updateGeometry();

					t = bas;
				}
				else {

					points++;

					// randomize target size (overriding BS requirement values)
					double widthNmi = (randomGenerator.nextDouble() * 2.0) + 1.0;
					double lengthNmi = (randomGenerator.nextDouble() * 4.0) + 1.0;

					if (widthNmi > lengthNmi) {
						final double temp = widthNmi;
						widthNmi = lengthNmi;
						lengthNmi = temp;
					}
					final double azDeg = randomGenerator.nextDouble() * 360.0;

					t = new PointTarget(
							String.format(	"P%09d",
											points), // target id
							targetName,
							null, // description
							lat,
							lon,
							Length.fromKilometers(alt)
									.ft(),
							lengthNmi, // major axis
							widthNmi, // minor axis
							azDeg,
							cc,
							gr,
							null); // order of battle
				}

				logger.debug(t);

				targets.add(t);

				if ((targets.size() % 1000) == 0) {
					logger.debug("Target Count: " + targets.size());
				}
			}

			br.close();

			logger.info("Point targets: " + points);
			logger.info("DSA targets: " + dsas);
			logger.info("LOC targets: " + locs);
			logger.info("BAS targets: " + numBas);
		}
		catch (final FileNotFoundException e) {
			logger.error(	"Unable to locate file: " + e.getLocalizedMessage(),
							e);
		}
		catch (final IOException e) {
			logger.error(	"Problem working with file: " + e.getLocalizedMessage(),
							e);
		}

		logger.info("Total targets: " + targets.size());

		// TODO: Randomize targets if not all of them
		// MAX_TARGETS

		// SAVE targets
		logger.info("Saving targets...");

		targetRepository.saveAll(targets);

		logger.info("InitTargets complete.");
	}

	public static List<GeodeticPoint> makePoly(
			final GeodeticPoint center,
			final Length y,
			final Length x ) {

		final List<GeodeticPoint> poly = new ArrayList<>();

		final GeodeticPoint ul = center.move(	Angle.fromDegrees(270),
												x.dividedBy(2.0))
				.move(	Angle.fromDegrees(0),
						y.dividedBy(2.0));

		final GeodeticPoint ur = ul.move(	Angle.fromDegrees(90),
											x);
		final GeodeticPoint lr = ur.move(	Angle.fromDegrees(180),
											y);
		final GeodeticPoint ll = lr.move(	Angle.fromDegrees(270),
											x);

		poly.add(ul);
		poly.add(ur);
		poly.add(lr);
		poly.add(ll);

		return poly;
	}

	private static InputStream openInputStream(
			final String fileName ) {

		if (fileName == null) {
			return null;
		}

		InputStream input = null;
		try {
			final File f = new File(
					fileName);

			if (f.getName()
					.toLowerCase()
					.endsWith("gz")) {
				input = new GZIPInputStream(
						new FileInputStream(
								f));
			}
			else {
				input = new FileInputStream(
						f);
			}
		}
		catch (final Exception ex) {
			logger.error("Exception trying to open file: " + fileName);
		}

		return input;
	}

	@Override
	public void run(
			final String... args )
			throws Exception {
		loadProperties();

		initTargets(targetFile);
	}

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				InitTargets.class).properties("spring.config.name:inittargets")
						.build()
						.run(args);
	}
}
