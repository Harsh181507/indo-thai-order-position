package com.pm.orderupdateservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order")
public record OrderServiceProperties(
        String inputFile,
        Publisher publisher
) {

    public record Publisher(
            int maxEventsPerSecond
    ) {
    }
}