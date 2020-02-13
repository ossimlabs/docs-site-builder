package com.maxar.terrain;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import com.radiantblue.analytics.core.akka.ClusterSystem;

@EnableDiscoveryClient
@SpringBootApplication
public class TerrainServiceApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:terrainms";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				TerrainServiceApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(TerrainServiceApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}

	@Bean
	public ClusterSystem clusterSystem() {
		return new ClusterSystem();
	}
}
