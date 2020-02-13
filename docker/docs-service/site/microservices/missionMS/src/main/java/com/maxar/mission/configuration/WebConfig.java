package com.maxar.mission.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.maxar.common.czml.CzmlHttpMessageConverter;
import com.maxar.common.czml.CzmlTypeHandler;
import com.maxar.common.czml.VehiclePositionCzmlProperties;
import com.maxar.common.handlers.VehiclePositionCzmlTypeHandler;
import com.maxar.common.handlers.VehiclePositionListCzmlTypeHandler;
import com.maxar.mission.czml.MissionCzmlTypeHandler;
import com.maxar.mission.czml.TrackCzmlTypeHandler;
import com.maxar.mission.model.czml.TrackCzmlProperties;

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
	public CzmlTypeHandler trackHandler() {
		return new TrackCzmlTypeHandler();
	}
	
	@Bean
	public CzmlTypeHandler missionHandler() {
		return new MissionCzmlTypeHandler();
	}
	
	@Bean
	public CzmlTypeHandler vehiclePositionHandler() {
		return new VehiclePositionCzmlTypeHandler();
	}
	
	@Bean
	public CzmlTypeHandler vehiclePositionListHandler() {
		return new VehiclePositionListCzmlTypeHandler();
	}
	
	@Bean
	@ConfigurationProperties(prefix = "czml.track")
	public TrackCzmlProperties trackCzmlProperties() {
		return new TrackCzmlProperties();
	}
	
	@Bean
	@ConfigurationProperties(prefix = "czml.vehicle")
	public VehiclePositionCzmlProperties vehiclePositionCzmlProperties() {
		return new VehiclePositionCzmlProperties();
	}
}
