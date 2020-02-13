package com.maxar.asset.common.configuration;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.maxar.ephemeris.model.VCMModel;
import com.radiantblue.afspc.astrostds.AstroStdsLibConfig;
import com.radiantblue.afspc.astrostds.mechanics.orbit.SPPropagator;

@Configuration
@PropertySource("classpath:sppropagator.properties")
public class SpPropagatorConfig
{
	private static final String NATIVE_LIB_CONFIG_KEY = "astroStdsLib.";
	private static final String USE_ASTRO_STDS_LIB_KEY = NATIVE_LIB_CONFIG_KEY + "useAstroStdsLib";
	private static final String LOCATION_KEY = NATIVE_LIB_CONFIG_KEY + "location";
	private static final String GEODIR_KEY = NATIVE_LIB_CONFIG_KEY + "geodir";
	private static final String BUF_SIZE_KEY = NATIVE_LIB_CONFIG_KEY + "bufSize";
	private static final String RUN_MODE_KEY = NATIVE_LIB_CONFIG_KEY + "runMode";
	private static final String SAVE_PARTIALS_KEY = NATIVE_LIB_CONFIG_KEY + "savePartials";
	private static final String IS_SPECTR_KEY = NATIVE_LIB_CONFIG_KEY + "isSpectr";
	private static final String CONSIDER_KEY = NATIVE_LIB_CONFIG_KEY + "consider";
	private static final String DECAY_ALT_KEY = NATIVE_LIB_CONFIG_KEY + "decayAlt";

	@Value("${propagator.sp.cacheTimeoutMinutes}")
	private Long spCacheTimeoutMinutes;

	@Value("${propagator.sp.cacheMaxSize}")
	private Integer spCacheMaxSize;

	@Value("${astroStdsLib.location}")
	private String location;

	@Value("${astroStdsLib.useAstroStdsLib}")
	private String useAstroStdsLib;

	@Value("${astroStdsLib.geodir}")
	private String geodir;

	@Value("${astroStdsLib.bufSize}")
	private Integer bufSize;

	@Value("${astroStdsLib.runMode}")
	private Integer runMode;

	@Value("${astroStdsLib.savePartials}")
	private Integer savePartials;

	@Value("${astroStdsLib.isSpectr}")
	private Integer isSpectr;

	@Value("${astroStdsLib.consider}")
	private Double consider;

	@Value("${astroStdsLib.decayAlt}")
	private Integer decayAlt;

	@Bean
	public Cache<VCMModel, SPPropagator> spPropagatorCache() {
		final Properties properties = new Properties();

		properties.put(	USE_ASTRO_STDS_LIB_KEY,
						useAstroStdsLib);
		properties.put(	LOCATION_KEY,
						location);
		properties.put(	GEODIR_KEY,
						geodir);
		properties.put(	BUF_SIZE_KEY,
						bufSize);
		properties.put(	RUN_MODE_KEY,
						runMode);
		properties.put(	SAVE_PARTIALS_KEY,
						savePartials);
		properties.put(	IS_SPECTR_KEY,
						isSpectr);
		properties.put(	CONSIDER_KEY,
						consider);
		properties.put(	DECAY_ALT_KEY,
						decayAlt);

		AstroStdsLibConfig.configureWithProperties(properties);

		return CacheBuilder.newBuilder()
				.maximumSize(spCacheMaxSize)
				.expireAfterWrite(	spCacheTimeoutMinutes,
									TimeUnit.MINUTES)
				.removalListener(new RemovalListener<VCMModel, SPPropagator>() {
					@Override
					public void onRemoval(
							final RemovalNotification<VCMModel, SPPropagator> notification ) {
						notification.getValue()
								.close();
					}
				})
				.build();
	}
}
