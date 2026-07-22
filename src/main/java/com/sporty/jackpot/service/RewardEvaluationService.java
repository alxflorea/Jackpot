package com.sporty.jackpot.service;

import com.sporty.jackpot.domain.Bet;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotReward;
import com.sporty.jackpot.repository.BetRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.repository.JackpotRewardRepository;
import com.sporty.jackpot.strategy.reward.RewardStrategyResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.random.RandomGenerator;

/**
 * Evaluates whether a contributing bet wins the jackpot reward. Each bet is
 * evaluated exactly once; repeated calls return the recorded outcome instead
 * of drawing again, so the endpoint cannot be farmed for extra chances.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RewardEvaluationService {

    private final BetRepository betRepository;
    private final JackpotRepository jackpotRepository;
    private final JackpotRewardRepository rewardRepository;
    private final RewardStrategyResolver strategyResolver;
    private final RandomGenerator random;

    @Transactional
    public RewardEvaluationResult evaluate(String betId) {
        Bet bet = betRepository.findById(betId)
                .orElseThrow(() -> new BetNotFoundException(betId));

        if (bet.isRewardEvaluated()) {
            return rewardRepository.findByBetId(betId)
                    .map(r -> new RewardEvaluationResult(
                            betId, bet.getJackpotId(), true, r.getJackpotRewardAmount(), true))
                    .orElseGet(() -> new RewardEvaluationResult(
                            betId, bet.getJackpotId(), false, null, true));
        }

        Jackpot jackpot = jackpotRepository.findById(bet.getJackpotId())
                .orElseThrow(() -> new IllegalStateException(
                        "Jackpot " + bet.getJackpotId() + " missing for contributing bet " + betId));

        BigDecimal winChance = strategyResolver
                .resolve(jackpot.getRewardConfig().getStrategyType())
                .winChance(jackpot);

        bet.setRewardEvaluated(true);
        betRepository.save(bet);

        boolean winner = BigDecimal.valueOf(random.nextDouble()).compareTo(winChance) < 0;
        if (!winner) {
            log.info("Bet {} did not win jackpot {} (chance was {})", betId, jackpot.getId(), winChance);
            return RewardEvaluationResult.loss(betId, jackpot.getId());
        }

        BigDecimal rewardAmount = jackpot.getCurrentPoolValue();
        rewardRepository.save(JackpotReward.builder()
                .betId(betId)
                .userId(bet.getUserId())
                .jackpotId(jackpot.getId())
                .jackpotRewardAmount(rewardAmount)
                .build());

        jackpot.resetPool();
        jackpotRepository.save(jackpot);

        log.info("Bet {} WON jackpot {}: {} (chance was {}); pool reset to {}",
                betId, jackpot.getId(), rewardAmount, winChance, jackpot.getInitialPoolValue());
        return RewardEvaluationResult.win(betId, jackpot.getId(), rewardAmount);
    }
}
