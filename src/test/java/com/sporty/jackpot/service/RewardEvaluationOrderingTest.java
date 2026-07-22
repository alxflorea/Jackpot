package com.sporty.jackpot.service;

import com.sporty.jackpot.domain.Bet;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotRewardConfig;
import com.sporty.jackpot.domain.RewardStrategyType;
import com.sporty.jackpot.repository.BetRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.repository.JackpotRewardRepository;
import com.sporty.jackpot.strategy.reward.RewardStrategyResolver;
import com.sporty.jackpot.strategy.reward.VariableGrowthRewardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * A jackpot's win chance and payout are always computed from its LIVE pool
 * value at evaluation time, never from the pool as it stood when a given bet
 * contributed (that figure is only kept as an audit snapshot on
 * jackpot_contribution and is never read back). This matches how a real
 * progressive jackpot behaves -- whoever's check lands first takes whatever
 * is in the pot right then, and the pot resets for everyone -- but it means a
 * bet's odds/payout depend on the ORDER reward-evaluation calls happen to
 * arrive in, not only on its own contribution history.
 */
@ExtendWith(MockitoExtension.class)
class RewardEvaluationOrderingTest {

    @Mock
    private BetRepository betRepository;
    @Mock
    private JackpotRepository jackpotRepository;
    @Mock
    private JackpotRewardRepository rewardRepository;
    @Mock
    private RandomGenerator random;

    private RewardEvaluationService service;
    private Jackpot jackpot;

    @BeforeEach
    void setUp() {
        service = new RewardEvaluationService(
                betRepository, jackpotRepository, rewardRepository,
                new RewardStrategyResolver(List.of(new VariableGrowthRewardStrategy())),
                random);

        // Both bets contributed while the pool was at 2500:
        // chance = 5% + 20% * (2500-1000)/1000 = 35%.
        jackpot = Jackpot.builder()
                .id("JP-1")
                .initialPoolValue(new BigDecimal("1000"))
                .currentPoolValue(new BigDecimal("2500"))
                .rewardConfig(JackpotRewardConfig.builder()
                        .strategyType(RewardStrategyType.VARIABLE_GROWTH)
                        .baseChancePercentage(new BigDecimal("0.0500"))
                        .growthRate(new BigDecimal("0.2000"))
                        .build())
                .build();

        // Same jackpot row is returned (and mutated in place) for both bets,
        // exactly as a fresh findById would after bet A's transaction commits.
        when(jackpotRepository.findById("JP-1")).thenReturn(Optional.of(jackpot));
    }

    @Test
    void aLaterBetIsJudgedAgainstTheAlreadyResetPoolIfAnEarlierBetWonFirst() {
        Bet betA = Bet.builder().id("bet-A").userId("user-A").jackpotId("JP-1").amount(new BigDecimal("50")).build();
        Bet betB = Bet.builder().id("bet-B").userId("user-B").jackpotId("JP-1").amount(new BigDecimal("50")).build();
        when(betRepository.findById("bet-A")).thenReturn(Optional.of(betA));
        when(betRepository.findById("bet-B")).thenReturn(Optional.of(betB));

        // 0.10 wins at the 35% chance the pool offers before any reset.
        // 0.20 would ALSO have won at 35% -- but loses at the 5% chance the
        // pool offers once bet A's win has already reset it.
        when(random.nextDouble()).thenReturn(0.10, 0.20);

        RewardEvaluationResult resultA = service.evaluate("bet-A");
        assertThat(resultA.winner()).isTrue();
        assertThat(resultA.rewardAmount()).isEqualByComparingTo("2500");
        assertThat(jackpot.getCurrentPoolValue()).isEqualByComparingTo("1000"); // reset by A's win

        RewardEvaluationResult resultB = service.evaluate("bet-B");

        // Bet B contributed to the same 2500 pool as bet A and would have won
        // under that pool's 35% chance -- but by the time it's evaluated, bet
        // A already claimed and reset the jackpot, so bet B is judged against
        // the reset pool's 5% chance instead, and loses.
        assertThat(resultB.winner()).isFalse();
        assertThat(resultB.rewardAmount()).isNull();
    }
}
