package com.maxar.target.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.maxar.common.csv.CsvHttpMessageConverter;
import com.maxar.common.czml.CzmlHttpMessageConverter;
import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.target.czml.TargetCzmlTypeHandler;
import com.maxar.target.czml.TargetListCzmlTypeHandler;
import com.maxar.target.czv.TargetCsvTypeHandler;
import com.maxar.target.model.czml.TargetCzmlProperties;

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
	public CzmlTypeHandler targetCzmlHandler() {
		return new TargetCzmlTypeHandler();
	}

	@Bean
	public CzmlTypeHandler targetListCzmlHandler() {
		return new TargetListCzmlTypeHandler();
	}

	@Bean
	public CsvHttpMessageConverter csvHttpMessageConverter() {
		return new CsvHttpMessageConverter();
	}

	@Bean
	public TargetCsvTypeHandler targetCsvHandler() {
		return new TargetCsvTypeHandler();
	}

	@Bean
	@ConfigurationProperties(prefix = "czml.target")
	public TargetCzmlProperties targetCzmlProperties() {
		return new TargetCzmlProperties();
	}
}
