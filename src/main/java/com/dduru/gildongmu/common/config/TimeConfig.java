package com.dduru.gildongmu.common.config;

import com.dduru.gildongmu.common.time.KoreaTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.system(KoreaTime.ZONE_ID);
    }
}
