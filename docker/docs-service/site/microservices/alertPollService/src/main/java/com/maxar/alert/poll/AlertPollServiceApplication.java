package com.maxar.alert.poll;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
public class AlertPollServiceApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:alert-poll";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				AlertPollServiceApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return createSpringApplicationBuilder().sources(AlertPollServiceApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	@LoadBalanced
	public RestTemplate discoveryRestTemplate() {
		return new RestTemplate();
	}
}
