package com.maxar.weather.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.maxar.common.czml.CzmlHttpMessageConverter;
import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.weather.czml.WeatherByDateCzmlTypeHandler;
import com.maxar.weather.model.weather.czml.WeatherByDateCzmlProperties;

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
	public CzmlTypeHandler weatherByDateHandler() {
		return new WeatherByDateCzmlTypeHandler();
	}
	
	@Bean
	@ConfigurationProperties(prefix = "czml.weather")
	public WeatherByDateCzmlProperties weatherByDateCzmlProperties() {
		return new WeatherByDateCzmlProperties();
	}
}
