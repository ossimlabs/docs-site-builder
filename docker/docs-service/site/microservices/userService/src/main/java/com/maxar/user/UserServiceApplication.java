package com.maxar.user;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableDiscoveryClient
@EnableJpaRepositories("com.maxar.user.repository")
@EntityScan("com.maxar.user.entity")
@SpringBootApplication
public class UserServiceApplication extends
		SpringBootServletInitializer
{
	private static final String DEFAULT_PROPERTIES = "spring.config.name:user";

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				UserServiceApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(UserServiceApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}
}
