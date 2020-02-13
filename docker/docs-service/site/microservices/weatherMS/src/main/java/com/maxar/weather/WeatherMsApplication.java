package com.maxar.weather;

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
	"com.maxar.weather.controller",
	"com.maxar.weather.configuration",
	"com.maxar.microservice.db.utils.spatial"
})
@EnableJpaRepositories("com.maxar.weather.repository")
@EntityScan("com.maxar.weather.entity")
public class WeatherMsApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:weatherms";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				WeatherMsApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(WeatherMsApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}
}
