package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.RewardStrategyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Fixed chance to win, independent of the pool state. */
@Component
public class FixedRewardStrategy implements RewardStrategy {

    @Override
    public RewardStrategyType type() {
        return RewardStrategyType.FIXED;
    }

    @Override
    public BigDecimal winChance(Jackpot jackpot) {
        return jackpot.getRewardConfig().getBaseChancePercentage();
    }
}
