package com.maxar.access.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.maxar.access.csv.AccessCsvTypeHandler;
import com.maxar.access.czml.AccessCzmlTypeHandler;
import com.maxar.access.czml.AccessListCzmlTypeHandler;
import com.maxar.access.model.czml.AccessCzmlProperties;
import com.maxar.common.csv.CsvHttpMessageConverter;
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
							CzmlHttpMessageConverter.APPLICATION_CZML)
				.mediaType(	"csv",
							CsvHttpMessageConverter.TEXT_CSV);
	}

	@Bean
	public CzmlHttpMessageConverter czmlHttpMessageConverter() {
		return new CzmlHttpMessageConverter();
	}

	@Bean
	public CzmlTypeHandler accessCzmlHandler() {
		return new AccessCzmlTypeHandler();
	}
	
	@Bean
	public CzmlTypeHandler accessListHandler() {
		return new AccessListCzmlTypeHandler();
	}
	
	@Bean
	public CsvHttpMessageConverter csvHttpMessageConverter() {
		return new CsvHttpMessageConverter();
	}

	@Bean
	public AccessCsvTypeHandler accessCsvHandler() {
		return new AccessCsvTypeHandler();
	}


	@Bean
	@ConfigurationProperties(prefix = "czml.access")
	public AccessCzmlProperties accessCzmlProperties() {
		return new AccessCzmlProperties();
	}
}
