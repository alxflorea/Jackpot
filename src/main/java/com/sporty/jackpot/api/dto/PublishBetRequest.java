package com.sporty.jackpot.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PublishBetRequest(
        @NotBlank @Schema(example = "bet-1") String betId,
        @NotBlank @Schema(example = "user-1") String userId,
        @NotBlank @Schema(example = "JP-FIXED", description = "Must match an existing jackpot; seeded IDs are JP-FIXED and JP-PROGRESSIVE") String jackpotId,
        @NotNull @Positive @Schema(example = "100.00") BigDecimal betAmount
) {
}
