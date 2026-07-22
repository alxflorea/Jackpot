package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotRewardConfig;
import com.sporty.jackpot.domain.RewardStrategyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Chance starts at the base value and grows as the pool grows:
 *
 * <pre>chance = min(1, base + growthRate * poolGrowthFactor)</pre>
 *
 * where poolGrowthFactor = (currentPool - initialPool) / initialPool. If the
 * pool has reached its configured limit, the chance is 100%.
 */
@Component
public class VariableGrowthRewardStrategy implements RewardStrategy {

    @Override
    public RewardStrategyType type() {
        return RewardStrategyType.VARIABLE_GROWTH;
    }

    @Override
    public BigDecimal winChance(Jackpot jackpot) {
        // If a pool limit is configured and breached, trigger an immediate jackpot win
        if (jackpot.getPoolLimit() != null
                && jackpot.getCurrentPoolValue().compareTo(jackpot.getPoolLimit()) >= 0) {
            return BigDecimal.ONE;
        }

        JackpotRewardConfig config = jackpot.getRewardConfig();

        BigDecimal growthFactor = jackpot.getCurrentPoolValue()
                .subtract(jackpot.getInitialPoolValue())
                .divide(jackpot.getInitialPoolValue(), 8, RoundingMode.HALF_UP);

        return config.getBaseChancePercentage()
                .add(config.getGrowthRate().multiply(growthFactor))
                .min(BigDecimal.ONE);
    }
}
