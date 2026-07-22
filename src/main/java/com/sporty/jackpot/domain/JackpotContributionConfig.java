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
 * Reusable contribution configuration. Percentages are stored as fractions
 * (e.g. 0.0200 = 2%). {@code id} is a natural key (e.g. "FIXED_5_PERCENT"),
 * not a generated surrogate.
 */
@Entity
@Table(name = "jackpot_contribution_config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JackpotContributionConfig {

    @Id
    @Column(length = 50)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy_type", nullable = false, length = 50)
    private ContributionStrategyType strategyType;

    @Column(name = "base_percentage", nullable = false, precision = 5, scale = 4)
    private BigDecimal basePercentage;

    /** Only used by VARIABLE_DECAY. */
    @Column(name = "decay_rate", precision = 5, scale = 4)
    private BigDecimal decayRate;

    /** Floor for the decayed percentage; only used by VARIABLE_DECAY. */
    @Column(name = "min_percentage", precision = 5, scale = 4)
    private BigDecimal minPercentage;
}
