package com.sporty.jackpot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Reusable reward configuration. Chances are stored as fractions
 * (e.g. 0.0010 = 0.1%). {@code id} is a natural key (e.g. "FIXED_CHANCE_10_PERCENT"),
 * not a generated surrogate.
 */
@Entity
@Table(name = "jackpot_reward_config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JackpotRewardConfig {

    @Id
    @Column(length = 50)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy_type", nullable = false, length = 50)
    private RewardStrategyType strategyType;

    @Column(name = "base_chance_percentage", nullable = false, precision = 5, scale = 4)
    private BigDecimal baseChancePercentage;

    /** Only used by VARIABLE_GROWTH. */
    @Column(name = "growth_rate", precision = 5, scale = 4)
    private BigDecimal growthRate;
}
