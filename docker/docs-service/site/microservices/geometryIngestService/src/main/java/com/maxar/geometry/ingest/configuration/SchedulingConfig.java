package com.maxar.geometry.ingest.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConditionalOnProperty(value = "geometry-ingest.scheduling.enable", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableScheduling
public class SchedulingConfig
{
}
