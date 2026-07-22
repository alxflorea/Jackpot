package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.domain.ContributionStrategyType;
import com.sporty.jackpot.domain.Jackpot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Contribution is a fixed percentage of the bet amount. */
@Component
public class FixedContributionStrategy implements ContributionStrategy {

    @Override
    public ContributionStrategyType type() {
        return ContributionStrategyType.FIXED;
    }

    @Override
    public BigDecimal calculateContribution(BigDecimal betAmount, Jackpot jackpot) {
        return betAmount
                .multiply(jackpot.getContributionConfig().getBasePercentage())
                .setScale(4, RoundingMode.HALF_UP);
    }
}
