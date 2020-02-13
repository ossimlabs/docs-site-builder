package com.maxar.geometric.intersection.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.maxar.common.czml.CzmlHttpMessageConverter;
import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.geometric.intersection.czml.AreaOfInterestCzmlProducer;
import com.maxar.geometric.intersection.czml.AreaOfInterestCzmlProperties;
import com.maxar.geometric.intersection.czml.AreaOfInterestCzmlTypeHandler;

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
	public CzmlTypeHandler areaOfInterestHandler() {
		return new AreaOfInterestCzmlTypeHandler();
	}

	@Bean
	public AreaOfInterestCzmlProducer areaOfInterestCzmlProducer() {
		return new AreaOfInterestCzmlProducer();
	}

	@Bean
	@ConfigurationProperties(prefix = "czml.aoi")
	public AreaOfInterestCzmlProperties areaOfInterestCzmlProperties() {
		return new AreaOfInterestCzmlProperties();
	}
}
