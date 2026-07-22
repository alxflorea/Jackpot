package com.sporty.jackpot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "jackpot")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Jackpot {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "initial_pool_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal initialPoolValue;

    @Column(name = "current_pool_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentPoolValue;

    /** When reached, VARIABLE_GROWTH reward chance becomes 100%. Optional. */
    @Column(name = "pool_limit", precision = 19, scale = 4)
    private BigDecimal poolLimit;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "contribution_config_id", nullable = false)
    private JackpotContributionConfig contributionConfig;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "reward_config_id", nullable = false)
    private JackpotRewardConfig rewardConfig;

    /** Optimistic locking guard for concurrent pool updates. */
    @Version
    @Column(nullable = false)
    private long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void contribute(BigDecimal contributionAmount) {
        this.currentPoolValue = this.currentPoolValue.add(contributionAmount);
    }

    public void resetPool() {
        this.currentPoolValue = this.initialPoolValue;
    }
}
