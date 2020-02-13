package com.maxar.ephemeris;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class EphemerisMsApplication extends
		SpringBootServletInitializer
{

	private static final String DEFAULT_PROPERTIES = "spring.config.name:ephemerisms";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				EphemerisMsApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);

	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(EphemerisMsApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}
}
