package com.sporty.jackpot.messaging;

import com.sporty.jackpot.service.BetProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!mock")
@RequiredArgsConstructor
public class JackpotBetsConsumer {

    private final BetProcessingService betProcessingService;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onBet(BetMessage message) {
        betProcessingService.processBet(message);
    }
}
