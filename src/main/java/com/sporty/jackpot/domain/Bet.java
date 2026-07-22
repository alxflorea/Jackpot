package com.sporty.jackpot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bet")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bet {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "jackpot_id", nullable = false, length = 36)
    private String jackpotId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /**
     * True once the bet has been evaluated for a reward. A bet is evaluated
     * exactly once, so the endpoint cannot be retried until a win comes up.
     */
    @Column(name = "reward_evaluated", nullable = false)
    private boolean rewardEvaluated;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
