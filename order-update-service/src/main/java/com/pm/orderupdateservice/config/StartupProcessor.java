package com.pm.orderupdateservice.config;

import com.pm.orderupdateservice.service.OrderFileProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class StartupProcessor {

    @Bean
    @Profile("!test")
    CommandLineRunner processOrderFile(OrderFileProcessor orderFileProcessor) {
        return args -> orderFileProcessor.process();
    }
}