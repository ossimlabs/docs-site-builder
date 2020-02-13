package com.maxar.mission;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = {
	"com.maxar.mission.controller",
	"com.maxar.mission.configuration",
	"com.maxar.microservice.db.utils.spatial"
})
@EnableJpaRepositories("com.maxar.mission.repository")
@EntityScan("com.maxar.mission.entity")
public class MissionMsApplication extends
		SpringBootServletInitializer
{

	private static final String DEFAULT_PROPERTIES = "spring.config.name:missionms";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				MissionMsApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(MissionMsApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}
}
