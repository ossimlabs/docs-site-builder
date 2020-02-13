package com.maxar.cesium.server;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = {
	"com.maxar.cesium.server",
	"com.maxar.swagger"
})
public class CesiumServerApplication extends
		SpringBootServletInitializer
{
	private static String DEFAULT_PROPERTIES = "spring.config.name:cesiumserver";

	@Value("${czml.cesiumserver.sessionEvictionTimeMinutes}")
	private String sessionEvictionTimeMinutes;

	public static void main(
			final String[] args ) {
		new SpringApplicationBuilder(
				CesiumServerApplication.class).properties(DEFAULT_PROPERTIES)
						.build()
						.run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(
			final SpringApplicationBuilder builder ) {
		return builder.sources(CesiumServerApplication.class)
				.properties(DEFAULT_PROPERTIES);
	}

	@Bean
	public LoadingCache<String, List<JsonNode>> sessionCache() {
		return CacheBuilder.newBuilder()
				.expireAfterAccess(	Long.valueOf(sessionEvictionTimeMinutes),
									TimeUnit.MINUTES)
				.build(new CacheLoader<String, List<JsonNode>>() {
					@Override
					public List<JsonNode> load(
							final String sessionId )
							throws Exception {
						// Not loading anything on 'get'. Only load explicitly with 'put'
						return null;
					}
				});
	}
}
