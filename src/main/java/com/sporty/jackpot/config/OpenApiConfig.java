package com.sporty.jackpot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jackpotOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Jackpot Service API")
                .version("1.0.0")
                .description("""
                        Receives bets, publishes them to the jackpot-bets Kafka topic, \
                        applies jackpot pool contributions and evaluates bets for jackpot rewards."""));
    }
}
