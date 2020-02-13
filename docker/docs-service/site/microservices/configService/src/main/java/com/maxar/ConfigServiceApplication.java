package com.maxar;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@EnableDiscoveryClient
@SpringBootApplication
public class ConfigServiceApplication
{
	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				ConfigServiceApplication.class).build()
						.run(args);
	}
}
