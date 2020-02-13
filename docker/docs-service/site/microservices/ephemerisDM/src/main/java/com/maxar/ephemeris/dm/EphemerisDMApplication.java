package com.maxar.ephemeris.dm;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.maxar.ephemeris.dm.configuration.EphemerisProperties;

@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties(EphemerisProperties.class)
@EnableJpaRepositories("com.maxar.ephemeris.repository")
@EntityScan("com.maxar.ephemeris.entity")
public class EphemerisDMApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:ephemerisdm";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				EphemerisDMApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(EphemerisDMApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}
}
