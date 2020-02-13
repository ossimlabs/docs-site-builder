package com.maxar.alert.poll.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConditionalOnProperty(value = "alert-poll.scheduling.enable", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableScheduling
public class SchedulingConfig
{
}
