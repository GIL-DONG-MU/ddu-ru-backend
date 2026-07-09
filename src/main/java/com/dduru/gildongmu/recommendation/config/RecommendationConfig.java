package com.dduru.gildongmu.recommendation.config;

import com.dduru.gildongmu.recommendation.support.RecommendationAvailableDateMatcher;
import com.dduru.gildongmu.recommendation.support.RecommendationScoreCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecommendationConfig {

    @Bean
    public RecommendationAvailableDateMatcher recommendationAvailableDateMatcher() {
        return new RecommendationAvailableDateMatcher();
    }

    @Bean
    public RecommendationScoreCalculator recommendationScoreCalculator() {
        return new RecommendationScoreCalculator();
    }
}
