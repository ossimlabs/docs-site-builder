package com.maxar.discovery;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class DiscoveryServiceApplication
{
	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				DiscoveryServiceApplication.class).build()
						.run(args);
	}
}
