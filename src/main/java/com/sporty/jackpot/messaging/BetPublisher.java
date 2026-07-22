package com.sporty.jackpot.messaging;

/** Publishes bets to the jackpot-bets topic. */
public interface BetPublisher {

    void publish(BetMessage message);
}
