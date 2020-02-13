package com.maxar.target;

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
	"com.maxar.target.controller",
	"com.maxar.target.configuration",
	"com.maxar.microservice.db.utils.spatial"
})
@EnableJpaRepositories("com.maxar.target.repository")
@EntityScan("com.maxar.target.entity")
public class TargetMsApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:targetms";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				TargetMsApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(TargetMsApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}
}
