package com.maxar.planning.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.maxar.common.czml.CzmlHttpMessageConverter;
import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.planning.czml.CollectionWindowCzmlTypeHandler;
import com.maxar.planning.czml.CollectionWindowListCzmlTypeHandler;
import com.maxar.planning.czml.TaskingCzmlTypeHandler;
import com.maxar.planning.czml.TaskingListCzmlTypeHandler;
import com.maxar.planning.model.czml.ImageFrameCzmlProperties;
import com.maxar.planning.model.czml.TaskingCzmlProperties;

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
	public CzmlTypeHandler collectionWindowHandler() {
		return new CollectionWindowCzmlTypeHandler();
	}

	@Bean
	public CzmlTypeHandler collectionWindowListHandler() {
		return new CollectionWindowListCzmlTypeHandler();
	}
	
	@Bean
	@ConfigurationProperties(prefix = "czml.image-frame")
	public ImageFrameCzmlProperties imageFrameCzmlProperties() {
		return new ImageFrameCzmlProperties();
	}
	
	@Bean
	public CzmlTypeHandler taskingHandler() {
		return new TaskingCzmlTypeHandler();
	}

	@Bean
	public CzmlTypeHandler taskingListHandler() {
		return new TaskingListCzmlTypeHandler();
	}
	
	@Bean
	@ConfigurationProperties(prefix = "czml.tasking")
	public TaskingCzmlProperties taskingCzmlProperties() {
		return new TaskingCzmlProperties();
	}
}

