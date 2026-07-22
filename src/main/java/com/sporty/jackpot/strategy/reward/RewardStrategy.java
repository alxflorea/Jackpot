package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.RewardStrategyType;

import java.math.BigDecimal;

/**
 * Computes the chance (as a fraction in [0, 1]) that a bet wins the jackpot.
 * New reward behaviours are added by implementing this interface with a new type.
 */
public interface RewardStrategy {

    RewardStrategyType type();

    BigDecimal winChance(Jackpot jackpot);
}
