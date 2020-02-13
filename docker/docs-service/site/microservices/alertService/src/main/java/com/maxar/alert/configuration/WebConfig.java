package com.maxar.alert.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.maxar.alert.czml.AlertCzmlProducer;
import com.maxar.alert.czml.AlertCzmlProperties;
import com.maxar.alert.czml.AlertCzmlTypeHandler;
import com.maxar.common.czml.CzmlHttpMessageConverter;
import com.maxar.common.czml.CzmlTypeHandler;

@Configuration
public class WebConfig implements
		WebMvcConfigurer
{
	@Override
	public void configureContentNegotiation(
			final ContentNegotiationConfigurer configurer ) {
		configurer.favorPathExtension(true)
				.favorParameter(true)
				.ignoreAcceptHeader(true)
				.useRegisteredExtensionsOnly(true)
				.defaultContentType(MediaType.APPLICATION_JSON)
				.mediaType(	"json",
							MediaType.APPLICATION_JSON)
				.mediaType(	"czml",
							CzmlHttpMessageConverter.APPLICATION_CZML);
	}

	@Bean
	public CzmlHttpMessageConverter czmlHttpMessageConverter() {
		return new CzmlHttpMessageConverter();
	}

	@Bean
	public CzmlTypeHandler alertHandler() {
		return new AlertCzmlTypeHandler();
	}

	@Bean
	public AlertCzmlProducer alertCzmlProducer() {
		return new AlertCzmlProducer();
	}

	@Bean
	@ConfigurationProperties(prefix = "czml.alert.event")
	public AlertCzmlProperties alertCzmlProperties() {
		return new AlertCzmlProperties();
	}
}
