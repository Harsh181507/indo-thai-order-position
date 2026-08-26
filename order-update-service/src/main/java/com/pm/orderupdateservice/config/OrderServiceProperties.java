package com.pm.orderupdateservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@ConfigurationProperties(prefix = "order")
@Validated
public record OrderServiceProperties(
        @NotBlank String inputFile,
        @Valid Publisher publisher
) {

    public record Publisher(
            @Positive int maxEventsPerSecond
    ) {
    }
}