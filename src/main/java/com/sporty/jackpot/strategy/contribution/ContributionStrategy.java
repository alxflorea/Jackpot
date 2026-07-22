package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.domain.ContributionStrategyType;
import com.sporty.jackpot.domain.Jackpot;

import java.math.BigDecimal;

/**
 * Computes how much of a bet goes into a jackpot pool. New contribution
 * behaviours are added by implementing this interface with a new type.
 */
public interface ContributionStrategy {

    ContributionStrategyType type();

    /** Returns the contribution amount for the given bet, based on the jackpot's config and pool state. */
    BigDecimal calculateContribution(BigDecimal betAmount, Jackpot jackpot);
}
