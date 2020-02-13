package com.maxar.ephemeris.dm.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;

@ConfigurationProperties
public class EphemerisProperties
{
	@Getter
	private final Map<String, String> ingesterProperties = new HashMap<>();
}
