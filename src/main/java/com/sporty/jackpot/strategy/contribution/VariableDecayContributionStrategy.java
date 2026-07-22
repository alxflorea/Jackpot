package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.domain.ContributionStrategyType;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotContributionConfig;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Contribution starts at the base percentage and decreases at a fixed rate as
 * the pool grows:
 *
 * <pre>percentage = max(minPercentage, base - decayRate * poolGrowthFactor)</pre>
 *
 * where poolGrowthFactor = (currentPool - initialPool) / initialPool, i.e. how
 * many times the pool has grown beyond its initial value.
 */
@Component
public class VariableDecayContributionStrategy implements ContributionStrategy {

    @Override
    public ContributionStrategyType type() {
        return ContributionStrategyType.VARIABLE_DECAY;
    }

    @Override
    public BigDecimal calculateContribution(BigDecimal betAmount, Jackpot jackpot) {
        JackpotContributionConfig config = jackpot.getContributionConfig();

        BigDecimal growthFactor = jackpot.getCurrentPoolValue()
                .subtract(jackpot.getInitialPoolValue())
                .divide(jackpot.getInitialPoolValue(), 8, RoundingMode.HALF_UP);

        BigDecimal floor = config.getMinPercentage() != null
                ? config.getMinPercentage()
                : BigDecimal.ZERO;

        BigDecimal percentage = config.getBasePercentage()
                .subtract(config.getDecayRate().multiply(growthFactor))
                .max(floor);

        return betAmount.multiply(percentage).setScale(4, RoundingMode.HALF_UP);
    }
}
