package com.sporty.jackpot.strategy;

import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotRewardConfig;
import com.sporty.jackpot.domain.RewardStrategyType;
import com.sporty.jackpot.strategy.reward.FixedRewardStrategy;
import com.sporty.jackpot.strategy.reward.VariableGrowthRewardStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RewardStrategyTest {

    private final FixedRewardStrategy fixed = new FixedRewardStrategy();
    private final VariableGrowthRewardStrategy growth = new VariableGrowthRewardStrategy();

    private JackpotRewardConfig growthConfig() {
        return JackpotRewardConfig.builder()
                .strategyType(RewardStrategyType.VARIABLE_GROWTH)
                .baseChancePercentage(new BigDecimal("0.0500"))
                .growthRate(new BigDecimal("0.2000"))
                .build();
    }

    private Jackpot jackpot(JackpotRewardConfig config, String initial, String current, String limit) {
        return Jackpot.builder()
                .id("JP-1")
                .initialPoolValue(new BigDecimal(initial))
                .currentPoolValue(new BigDecimal(current))
                .poolLimit(limit != null ? new BigDecimal(limit) : null)
                .rewardConfig(config)
                .build();
    }

    @Test
    void fixedChanceIgnoresPoolState() {
        JackpotRewardConfig config = JackpotRewardConfig.builder()
                .strategyType(RewardStrategyType.FIXED)
                .baseChancePercentage(new BigDecimal("0.1000"))
                .build();

        assertThat(fixed.winChance(jackpot(config, "1000", "99999", null)))
                .isEqualByComparingTo("0.1000");
    }

    @Test
    void growthChanceStartsAtBaseWhenPoolIsAtInitialValue() {
        assertThat(growth.winChance(jackpot(growthConfig(), "5000", "5000", "10000")))
                .isEqualByComparingTo("0.0500");
    }

    @Test
    void growthChanceIncreasesAsThePoolGrows() {
        // Pool grew 50%: chance = 0.05 + 0.20 * 0.5 = 0.15
        assertThat(growth.winChance(jackpot(growthConfig(), "5000", "7500", "10000")))
                .isEqualByComparingTo("0.1500");
    }

    @Test
    void growthChanceBecomes100PercentWhenPoolHitsTheLimit() {
        assertThat(growth.winChance(jackpot(growthConfig(), "5000", "10000", "10000")))
                .isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void growthChanceIsCappedAtOneEvenWithoutALimit() {
        // Enormous growth, no limit configured -> still never above 1
        assertThat(growth.winChance(jackpot(growthConfig(), "5000", "500000", null)))
                .isEqualByComparingTo(BigDecimal.ONE);
    }
}
