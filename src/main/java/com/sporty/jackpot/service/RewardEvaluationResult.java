package com.sporty.jackpot.service;

import java.math.BigDecimal;

/** Outcome of evaluating a bet for a jackpot reward. */
public record RewardEvaluationResult(
        String betId,
        String jackpotId,
        boolean winner,
        BigDecimal rewardAmount,
        boolean alreadyEvaluated
) {

    public static RewardEvaluationResult win(String betId, String jackpotId, BigDecimal amount) {
        return new RewardEvaluationResult(betId, jackpotId, true, amount, false);
    }

    public static RewardEvaluationResult loss(String betId, String jackpotId) {
        return new RewardEvaluationResult(betId, jackpotId, false, null, false);
    }
}
