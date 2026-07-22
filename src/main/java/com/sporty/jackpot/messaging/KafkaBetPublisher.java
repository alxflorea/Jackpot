package com.sporty.jackpot.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!mock")
@RequiredArgsConstructor
@Slf4j
public class KafkaBetPublisher implements BetPublisher {

    private final KafkaTemplate<String, BetMessage> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Override
    public void publish(BetMessage message) {
        // Key by jackpotId so all bets of one jackpot land on the same
        // partition and are processed in order.
        kafkaTemplate.send(topic, message.jackpotId(), message);
        log.info("Published bet {} to topic {}", message.betId(), topic);
    }
}
