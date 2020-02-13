package com.maxar.alert;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(basePackages = {
	"com.maxar.alert",
	"com.maxar.microservice.db.utils.spatial"
})
@EnableDiscoveryClient
@EnableJpaRepositories("com.maxar.alert.repository")
@EntityScan("com.maxar.alert.entity")
@SpringBootApplication
public class AlertServiceApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:alert";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				AlertServiceApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(AlertServiceApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}
}
