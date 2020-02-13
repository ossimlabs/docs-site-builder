package com.maxar.weather.dm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.maxar.weather.dm.configuration.WeatherProperties;
import com.maxar.weather.dm.manager.WeatherDataManager;

@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = {
	"com.maxar.weather.dm.manager"
})
@EnableConfigurationProperties(WeatherProperties.class)
@EnableJpaRepositories("com.maxar.weather.repository")
@EntityScan("com.maxar.weather.entity")
public class WeatherDMApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:weatherdm";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				WeatherDMApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(WeatherDMApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}

	@Autowired
	WeatherDataManager weatherDataManager;
}
