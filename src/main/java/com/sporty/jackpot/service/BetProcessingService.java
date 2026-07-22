package com.sporty.jackpot.service;

import com.sporty.jackpot.domain.Bet;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotContribution;
import com.sporty.jackpot.messaging.BetMessage;
import com.sporty.jackpot.repository.BetRepository;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.strategy.contribution.ContributionStrategyResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Consumes bets and applies the jackpot contribution. Idempotent: a bet that
 * was already processed (redelivered by Kafka) is skipped.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BetProcessingService {

    private final BetRepository betRepository;
    private final JackpotRepository jackpotRepository;
    private final JackpotContributionRepository contributionRepository;
    private final ContributionStrategyResolver strategyResolver;

    @Transactional
    public void processBet(BetMessage message) {
        if (betRepository.existsById(message.betId())) {
            log.info("Bet {} already processed, skipping (at-least-once redelivery)", message.betId());
            return;
        }

        Optional<Jackpot> matchingJackpot = jackpotRepository.findById(message.jackpotId());
        if (matchingJackpot.isEmpty()) {
            log.warn("No matching jackpot {} for bet {}, ignoring", message.jackpotId(), message.betId());
            return;
        }
        Jackpot jackpot = matchingJackpot.get();

        BigDecimal contributionAmount = strategyResolver
                .resolve(jackpot.getContributionConfig().getStrategyType())
                .calculateContribution(message.betAmount(), jackpot);

        jackpot.contribute(contributionAmount);
        jackpotRepository.save(jackpot);

        betRepository.save(Bet.builder()
                .id(message.betId())
                .userId(message.userId())
                .jackpotId(message.jackpotId())
                .amount(message.betAmount())
                .build());

        contributionRepository.save(JackpotContribution.builder()
                .betId(message.betId())
                .userId(message.userId())
                .jackpotId(message.jackpotId())
                .stakeAmount(message.betAmount())
                .contributionAmount(contributionAmount)
                .currentJackpotAmount(jackpot.getCurrentPoolValue())
                .build());

        log.info("Bet {} contributed {} to jackpot {} (pool now {})",
                message.betId(), contributionAmount, jackpot.getId(), jackpot.getCurrentPoolValue());
    }
}
