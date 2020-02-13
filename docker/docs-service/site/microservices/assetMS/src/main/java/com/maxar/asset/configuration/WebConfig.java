package com.maxar.asset.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.maxar.asset.czml.AssetSmearListCzmlTypeHandler;
import com.maxar.asset.czml.FieldOfRegardListCzmlTypeHandler;
import com.maxar.asset.model.czml.AssetSmearCzmlProperties;
import com.maxar.asset.model.czml.FieldOfRegardCzmlProperties;
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
	public CzmlTypeHandler fieldOfRegardListHandler() {
		return new FieldOfRegardListCzmlTypeHandler();
	}
	
	@Bean
	@ConfigurationProperties(prefix = "czml.field-of-regard")
	public FieldOfRegardCzmlProperties fieldOfRegardCzmlProperties() {
		return new FieldOfRegardCzmlProperties();
	}
	
	@Bean
	public CzmlTypeHandler assetSmearListHandler() {
		return new AssetSmearListCzmlTypeHandler();
	}

	@Bean
	@ConfigurationProperties(prefix = "czml.asset-smear")
	public AssetSmearCzmlProperties assetSmearCzmlProperties() {
		return new AssetSmearCzmlProperties();
	}
}
