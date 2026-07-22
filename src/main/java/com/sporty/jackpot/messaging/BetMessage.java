package com.sporty.jackpot.messaging;

import java.math.BigDecimal;

/** Payload published to and consumed from the jackpot-bets topic. */
public record BetMessage(
        String betId,
        String userId,
        String jackpotId,
        BigDecimal betAmount
) {
}
