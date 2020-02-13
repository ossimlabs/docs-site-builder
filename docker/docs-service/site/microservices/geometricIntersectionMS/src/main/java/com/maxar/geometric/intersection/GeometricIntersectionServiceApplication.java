package com.maxar.geometric.intersection;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableDiscoveryClient
@SpringBootApplication
@EnableJpaRepositories("com.maxar.geometric.intersection.repository")
@EntityScan("com.maxar.geometric.intersection.entity")
@ComponentScan(basePackages = {
	"com.maxar.geometric.intersection",
	"com.maxar.microservice.db.utils.spatial"
})
public class GeometricIntersectionServiceApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:geometricintersectionms";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				GeometricIntersectionServiceApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(GeometricIntersectionServiceApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}
}
