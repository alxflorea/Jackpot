package com.sporty.jackpot.strategy;

import com.sporty.jackpot.domain.ContributionStrategyType;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotContributionConfig;
import com.sporty.jackpot.strategy.contribution.FixedContributionStrategy;
import com.sporty.jackpot.strategy.contribution.VariableDecayContributionStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ContributionStrategyTest {

    private final FixedContributionStrategy fixed = new FixedContributionStrategy();
    private final VariableDecayContributionStrategy decay = new VariableDecayContributionStrategy();

    private Jackpot jackpot(JackpotContributionConfig config, String initial, String current) {
        return Jackpot.builder()
                .id("JP-1")
                .initialPoolValue(new BigDecimal(initial))
                .currentPoolValue(new BigDecimal(current))
                .contributionConfig(config)
                .build();
    }

    @Test
    void fixedContributionIsAConstantPercentageOfTheBet() {
        JackpotContributionConfig config = JackpotContributionConfig.builder()
                .strategyType(ContributionStrategyType.FIXED)
                .basePercentage(new BigDecimal("0.0500"))
                .build();

        BigDecimal contribution = fixed.calculateContribution(
                new BigDecimal("200.00"), jackpot(config, "1000", "8000"));

        assertThat(contribution).isEqualByComparingTo("10.0000");
    }

    @Test
    void decayContributionStartsAtBasePercentageWhenPoolIsAtInitialValue() {
        JackpotContributionConfig config = JackpotContributionConfig.builder()
                .strategyType(ContributionStrategyType.VARIABLE_DECAY)
                .basePercentage(new BigDecimal("0.1000"))
                .decayRate(new BigDecimal("0.0300"))
                .minPercentage(new BigDecimal("0.0100"))
                .build();

        BigDecimal contribution = decay.calculateContribution(
                new BigDecimal("100.00"), jackpot(config, "1000", "1000"));

        assertThat(contribution).isEqualByComparingTo("10.0000");
    }

    @Test
    void decayContributionDecreasesAsThePoolGrows() {
        JackpotContributionConfig config = JackpotContributionConfig.builder()
                .strategyType(ContributionStrategyType.VARIABLE_DECAY)
                .basePercentage(new BigDecimal("0.1000"))
                .decayRate(new BigDecimal("0.0300"))
                .minPercentage(new BigDecimal("0.0100"))
                .build();

        // Pool doubled: growthFactor = 1 -> pct = 0.10 - 0.03 = 0.07
        BigDecimal contribution = decay.calculateContribution(
                new BigDecimal("100.00"), jackpot(config, "1000", "2000"));

        assertThat(contribution).isEqualByComparingTo("7.0000");
    }

    @Test
    void decayContributionNeverDropsBelowTheConfiguredFloor() {
        JackpotContributionConfig config = JackpotContributionConfig.builder()
                .strategyType(ContributionStrategyType.VARIABLE_DECAY)
                .basePercentage(new BigDecimal("0.1000"))
                .decayRate(new BigDecimal("0.0300"))
                .minPercentage(new BigDecimal("0.0100"))
                .build();

        // Pool grew 10x: 0.10 - 0.03 * 9 would be negative -> floor of 1% applies
        BigDecimal contribution = decay.calculateContribution(
                new BigDecimal("100.00"), jackpot(config, "1000", "10000"));

        assertThat(contribution).isEqualByComparingTo("1.0000");
    }
}
