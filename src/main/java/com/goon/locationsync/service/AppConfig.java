package com.goon.locationsync.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record AppConfig(
    String locationUpdates,
    String driverLocations

) {}
