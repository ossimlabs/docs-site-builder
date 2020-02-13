package com.maxar.init.database.loaders.ephemeris;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.maxar.common.czml.VehiclePositionCZMLUtils;
import com.maxar.ephemeris.entity.StateVector;
import com.maxar.ephemeris.entity.StateVectorSet;
import com.maxar.ephemeris.entity.TLE;
import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.ephemeris.model.TLEModel;
import com.maxar.ephemeris.repository.StateVectorSetRepository;
import com.maxar.ephemeris.repository.TLERepository;
import com.maxar.init.database.loaders.ephemeris.utils.TLEImporter;
import com.radiantblue.analytics.core.Vector3D;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.mechanics.IStateVectorProvider;
import com.radiantblue.analytics.mechanics.orbit.J2Propagator;
import com.radiantblue.analytics.mechanics.orbit.ThreadSafeSGP4Propagator;
import com.radiantblue.analytics.mechanics.orbit.elementproviders.TLEElementProvider;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

@EnableAutoConfiguration
@ComponentScan(basePackages = {
	"com.maxar.ephemeris.controller",
	"com.maxar.ephemeris.configuration"
})
@EnableJpaRepositories("com.maxar.ephemeris.repository")
@EntityScan("com.maxar.ephemeris.entity")
public class InitEphemeris implements
		CommandLineRunner
{

	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	@Autowired
	private TLERepository tleRepository;

	@Autowired
	private StateVectorSetRepository svsRepository;

	static private String ApplicationName = "initephemeris";

	static private final long svSamplingInterval = Duration.standardMinutes(2)
			.getMillis();

	// Use 7 days as threshold to choose J2 or SGP4 propagator
	private static final long PROPAGATOR_TYPE_THRESHOLD_MILLIS = 7L * 24L * 60L * 60L * 1000L;

	@Value("${microservices.ephemeris.tleDirectory}")
	private String tleDirNameProp;

	@Value("#{'${microservices.ephemeris.tleScns}'.split(',')}")
	private List<Integer> tleScns;

	public void initTLEs() {
		String tleDirName = tleDirNameProp;

		if (!tleDirName.startsWith("/")) {
			final URL tleUrl = Thread.currentThread()
					.getContextClassLoader()
					.getResource(tleDirName);

			if (tleUrl == null) {
				throw new RuntimeException(
						"Cannot find resource on classpath: '" + tleDirName + "'");
			}

			tleDirName = tleUrl.getFile();
		}

		final File tleDir = new File(
				tleDirName);

		if (!tleDir.exists()) {
			logger.error(tleDirName + " does not exist");
		}

		final File[] tleFiles = tleDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(
					final File dir,
					final String name ) {
				return name.endsWith("tle");
			}
		});

		for (final Integer scn : tleScns) {
			logger.info("SCN configured: " + scn);
		}

		final char classFiller = 'U';

		for (final File file : tleFiles) {
			logger.info("TLE file found: " + file.getName());
			try (InputStream input = new FileInputStream(
					file)) {

				final BufferedReader br = new BufferedReader(
						new InputStreamReader(
								input));

				final List<TLE> tles = TLEImporter.parseTLEsFromReader(	tleScns,
																		br,
																		classFiller,
																		null);

				final List<StateVectorSet> svsList = new ArrayList<>();

				logger.info("Found " + tles.size() + " tles");

				br.close();

				final List<TLE> dbTles = tleRepository.saveAll(tles);

				// Convert TLEs to state vectors sets and save to repository
				for (final TLE dbTle : dbTles) {
					final StateVectorSet svs = new StateVectorSet();

					svs.setEphemerisKey(dbTle.getEphemerisKey());
					svs.setScn(dbTle.getScn());
					svs.setEpochMillis(dbTle.getEpochMillis());
					svs.setType(EphemerisType.STATE_VECTOR_SET);

					final TLEModel tleModel = (TLEModel) dbTle.toModel();

					final DateTime start = new DateTime(
							tleModel.getEpochMillis());

					final List<StateVectorsInFrame> svp = tleToStateVectors(start,
																			start.plusHours(24),
																			svSamplingInterval,
																			tleModel);

					for (final StateVectorsInFrame svif : svp) {
						final StateVector sv = new StateVector();
						
						sv.setStateVectorSet(svs);
						sv.setAtTimeMillis(svif.atTime().getMillis());

						final Vector3D pos = svif.getPosition();
						final Vector3D vel = svif.getVelocity();
						final Vector3D acc = new Vector3D(
								0.0,
								0.0,
								0.0);

						// ECI
						sv.setEciPosX(pos.x());
						sv.setEciPosY(pos.y());
						sv.setEciPosZ(pos.z());

						sv.setEciVelX(vel.x());
						sv.setEciVelY(vel.y());
						sv.setEciVelZ(vel.z());

						sv.setEciAccelX(acc.x());
						sv.setEciAccelY(acc.y());
						sv.setEciAccelZ(acc.z());

						// ECF
						sv.setEcfPosX(pos.x());
						sv.setEcfPosY(pos.y());
						sv.setEcfPosZ(pos.z());

						sv.setEcfVelX(vel.x());
						sv.setEcfVelY(vel.y());
						sv.setEcfVelZ(vel.z());

						sv.setEcfAccelX(acc.x());
						sv.setEcfAccelY(acc.y());
						sv.setEcfAccelZ(acc.z());

						svs.getStateVectors()
								.add(sv);
						sv.setStateVectorSet(svs);
					}

					svsList.add(svs);
				}

				svsRepository.saveAll(svsList);
			}
			catch (final IOException e) {
				logger.error("Error parsing tle file: " + file.getName());
				e.printStackTrace();
			}
		}
	}

	private static IStateVectorProvider tleToStateVectorProvider(
			final TLEModel tle,
			final DateTime svStart ) {
		final TLEElementProvider tleEp = new TLEElementProvider(
				tle.getDescription(),
				tle.getTleLineOne(),
				tle.getTleLineTwo());

		IStateVectorProvider svp;

		final long tleAgeMillis = svStart.getMillis() - tle.getEpochMillis();
		if (tleAgeMillis > PROPAGATOR_TYPE_THRESHOLD_MILLIS) {
			svp = new J2Propagator(
					tleEp);
		}
		else {
			svp = new ThreadSafeSGP4Propagator(
					tleEp);
		}

		return svp;
	}

	public static List<StateVectorsInFrame> tleToStateVectors(
			final DateTime start,
			final DateTime end,
			final long samplingInterval,
			final TLEModel tle ) {

		final IStateVectorProvider svp = tleToStateVectorProvider(	tle,
																	start);

		return VehiclePositionCZMLUtils.generatePositionVectors(start,
																end,
																samplingInterval,
																svp);
	}

	@Override
	public void run(
			final String... args )
			throws Exception {
		initTLEs();
	}

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				InitEphemeris.class).properties("spring.config.name:" + ApplicationName)
						.build()
						.run(args);
	}
}
