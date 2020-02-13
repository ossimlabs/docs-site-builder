package com.maxar.spaceobjectcatalog;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableDiscoveryClient
@EnableJpaRepositories("com.maxar.spaceobjectcatalog.repository")
@EntityScan("com.maxar.ephemeris.entity")
@SpringBootApplication
public class SpaceObjectCatalogServiceApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:space-object-catalog";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				SpaceObjectCatalogServiceApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return createSpringApplicationBuilder().sources(SpaceObjectCatalogServiceApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}
}
