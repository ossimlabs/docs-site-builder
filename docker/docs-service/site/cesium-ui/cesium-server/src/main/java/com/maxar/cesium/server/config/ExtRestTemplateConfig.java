package com.maxar.cesium.server.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@ConditionalOnProperty(value = "eureka.client.register-with-eureka", havingValue = "false", matchIfMissing = true)
@Configuration
public class ExtRestTemplateConfig
{
	@Bean
	@Qualifier("withEureka")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	@Qualifier("withoutEureka")
	public RestTemplate extRestTemplate() {
		return new RestTemplate();
	}
}
