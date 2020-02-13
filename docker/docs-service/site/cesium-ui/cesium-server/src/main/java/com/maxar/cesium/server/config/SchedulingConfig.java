package com.maxar.cesium.server.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConditionalOnProperty(value = "czml.cesiumserver.scheduling.enable", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableScheduling
public class SchedulingConfig
{
}
