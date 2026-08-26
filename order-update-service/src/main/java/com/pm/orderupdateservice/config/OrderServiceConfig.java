package com.pm.orderupdateservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OrderServiceProperties.class)
public class OrderServiceConfig {
}