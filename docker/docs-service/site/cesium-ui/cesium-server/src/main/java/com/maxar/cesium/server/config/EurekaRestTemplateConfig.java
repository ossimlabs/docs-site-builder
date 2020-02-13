package com.maxar.cesium.server.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@ConditionalOnProperty(value = "eureka.client.register-with-eureka", havingValue = "true")
@Configuration
public class EurekaRestTemplateConfig
{
	@Bean
	@LoadBalanced
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
