package com.dduru.gildongmu.common.config;

import jakarta.validation.ClockProvider;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ValidationConfig {

    @Bean
    public ValidationConfigurationCustomizer validationConfigurationCustomizer(Clock clock) {
        ClockProvider clockProvider = () -> clock;
        return configuration -> configuration.clockProvider(clockProvider);
    }
}
