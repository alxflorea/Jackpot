package com.sporty.jackpot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
import java.util.UUID;

@Entity
@Table(name = "jackpot_contribution")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JackpotContribution {

    @Id
    @Builder.Default
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "bet_id", nullable = false, unique = true, length = 36)
    private String betId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "jackpot_id", nullable = false, length = 36)
    private String jackpotId;

    @Column(name = "stake_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal stakeAmount;

    @Column(name = "contribution_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal contributionAmount;

    /** Snapshot of the jackpot pool right after this contribution was applied. */
    @Column(name = "current_jackpot_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentJackpotAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
