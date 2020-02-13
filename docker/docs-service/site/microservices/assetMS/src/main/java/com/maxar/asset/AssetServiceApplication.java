package com.maxar.asset;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaRepositories("com.maxar.asset.repository")
@EntityScan("com.maxar.asset.entity")
@ComponentScan(basePackages = {
	"com.maxar.asset.controller",
	"com.maxar.asset.configuration",
	"com.maxar.asset.service",
	"com.maxar.asset.common.service",
	"com.maxar.asset.common.configuration"
})
public class AssetServiceApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:assetms";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				AssetServiceApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(AssetServiceApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
