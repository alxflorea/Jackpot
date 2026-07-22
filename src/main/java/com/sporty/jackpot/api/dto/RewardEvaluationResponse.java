package com.sporty.jackpot.api.dto;

import com.sporty.jackpot.service.RewardEvaluationResult;

import java.math.BigDecimal;

public record RewardEvaluationResponse(
        String betId,
        String jackpotId,
        boolean winner,
        BigDecimal rewardAmount,
        boolean alreadyEvaluated
) {

    public static RewardEvaluationResponse from(RewardEvaluationResult result) {
        return new RewardEvaluationResponse(
                result.betId(),
                result.jackpotId(),
                result.winner(),
                result.rewardAmount(),
                result.alreadyEvaluated());
    }
}
