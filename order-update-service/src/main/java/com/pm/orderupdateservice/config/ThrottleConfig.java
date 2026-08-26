package com.pm.orderupdateservice.config;

import com.pm.orderupdateservice.throttle.EventThrottle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThrottleConfig {

    @Bean
    public EventThrottle eventThrottle(OrderServiceProperties properties) {
        return new EventThrottle(
                properties.publisher().maxEventsPerSecond()
        );
    }
}