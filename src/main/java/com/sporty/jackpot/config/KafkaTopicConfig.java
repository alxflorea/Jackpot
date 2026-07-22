package com.sporty.jackpot.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Profile("!mock")
@Slf4j
public class KafkaTopicConfig {

    @Bean
    public NewTopic jackpotBetsTopic(@Value("${app.kafka.topic}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    /**
     * Poison-pill records (malformed JSON, unmapped fields, etc.) and any other
     * listener failure are logged and skipped instead of blocking the partition
     * with endless retries.
     */
    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler((record, exception) -> log.error(
                "Discarding unprocessable record from topic {} partition {} offset {}: {}",
                record.topic(), record.partition(), record.offset(), record.value(), exception),
                new FixedBackOff(0L, 0L));
    }
}
