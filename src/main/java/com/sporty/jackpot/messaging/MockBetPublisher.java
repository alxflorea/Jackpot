package com.sporty.jackpot.messaging;

import com.sporty.jackpot.service.BetProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Mock for the Kafka producer (allowed by the assignment): logs the payload
 * and hands it straight to the processing service, simulating an immediate
 * consume. Lets the whole flow run without a Kafka broker.
 */
@Component
@Profile("mock")
@RequiredArgsConstructor
@Slf4j
public class MockBetPublisher implements BetPublisher {

    private final BetProcessingService betProcessingService;

    @Override
    public void publish(BetMessage message) {
        log.info("[MOCK KAFKA] Would publish to jackpot-bets: {}", message);
        betProcessingService.processBet(message);
    }
}
