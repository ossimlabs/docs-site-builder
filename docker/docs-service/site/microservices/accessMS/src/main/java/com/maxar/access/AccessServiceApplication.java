package com.maxar.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import com.maxar.common.handlers.RestTemplateErrorHandler;

@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = {
	"com.maxar.access",
	"com.maxar.asset.common.client.space",
	"com.maxar.asset.common.service",
	"com.maxar.asset.common.configuration"
})
public class AccessServiceApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:accessms";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				AccessServiceApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(AccessServiceApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}

	@Autowired
	RestTemplateBuilder restTemplateBuilder;

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return restTemplateBuilder.errorHandler(new RestTemplateErrorHandler())
				.build();
	}
}
