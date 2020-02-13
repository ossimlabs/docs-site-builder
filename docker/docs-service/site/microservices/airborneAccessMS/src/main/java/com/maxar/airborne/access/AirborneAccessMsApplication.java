package com.maxar.airborne.access;

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
	"com.maxar.airborne.access",
	"com.maxar.access.common",
	"com.maxar.asset.common.client.airborne",
	"com.maxar.asset.common.service"
})
public class AirborneAccessMsApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:airborneaccessms";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				AirborneAccessMsApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(AirborneAccessMsApplication.class)
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
