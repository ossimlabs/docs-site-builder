package com.maxar.planning;

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
	"com.maxar.planning.controller",
	"com.maxar.planning.configuration",
	"com.maxar.microservice.db.utils.spatial"
})
@EnableJpaRepositories("com.maxar.planning.repository")
@EntityScan("com.maxar.planning.entity")
public class PlanningMsApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:planningms";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				PlanningMsApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);

	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(PlanningMsApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}
}
