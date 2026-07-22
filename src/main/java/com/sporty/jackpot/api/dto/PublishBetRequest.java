package com.sporty.jackpot.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PublishBetRequest(
        @NotBlank String betId,
        @NotBlank String userId,
        @NotBlank String jackpotId,
        @NotNull @Positive BigDecimal betAmount
) {
}
