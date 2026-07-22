package com.sporty.jackpot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

@Configuration
public class AppConfig {

    /** Injected as an interface so tests can supply a deterministic generator. */
    @Bean
    public RandomGenerator randomGenerator() {
        return new SecureRandom();
    }
}
