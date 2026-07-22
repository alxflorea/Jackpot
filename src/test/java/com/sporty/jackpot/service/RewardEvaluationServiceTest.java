package com.sporty.jackpot.service;

import com.sporty.jackpot.domain.*;
import com.sporty.jackpot.repository.BetRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.repository.JackpotRewardRepository;
import com.sporty.jackpot.strategy.reward.FixedRewardStrategy;
import com.sporty.jackpot.strategy.reward.RewardStrategyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardEvaluationServiceTest {

    @Mock
    private BetRepository betRepository;
    @Mock
    private JackpotRepository jackpotRepository;
    @Mock
    private JackpotRewardRepository rewardRepository;
    @Mock
    private RandomGenerator random;

    private RewardEvaluationService service;

    @BeforeEach
    void setUp() {
        service = new RewardEvaluationService(
                betRepository, jackpotRepository, rewardRepository,
                new RewardStrategyResolver(List.of(new FixedRewardStrategy())),
                random);
    }

    private Bet bet() {
        return Bet.builder()
                .id("bet-1").userId("user-1").jackpotId("JP-1")
                .amount(new BigDecimal("100.00"))
                .build();
    }

    private Jackpot jackpotWithFixedChance(String chance) {
        return Jackpot.builder()
                .id("JP-1")
                .initialPoolValue(new BigDecimal("1000"))
                .currentPoolValue(new BigDecimal("2500"))
                .rewardConfig(JackpotRewardConfig.builder()
                        .strategyType(RewardStrategyType.FIXED)
                        .baseChancePercentage(new BigDecimal(chance))
                        .build())
                .build();
    }

    @Test
    void winningBetGetsTheWholePoolAndThePoolResets() {
        Jackpot jackpot = jackpotWithFixedChance("0.1000");
        when(betRepository.findById("bet-1")).thenReturn(Optional.of(bet()));
        when(jackpotRepository.findById("JP-1")).thenReturn(Optional.of(jackpot));
        when(random.nextDouble()).thenReturn(0.05); // below 10% chance -> win

        RewardEvaluationResult result = service.evaluate("bet-1");

        assertThat(result.winner()).isTrue();
        assertThat(result.rewardAmount()).isEqualByComparingTo("2500");
        assertThat(jackpot.getCurrentPoolValue()).isEqualByComparingTo("1000");

        ArgumentCaptor<JackpotReward> captor = ArgumentCaptor.forClass(JackpotReward.class);
        verify(rewardRepository).save(captor.capture());
        assertThat(captor.getValue().getJackpotRewardAmount()).isEqualByComparingTo("2500");
    }

    @Test
    void losingBetLeavesThePoolUntouched() {
        Jackpot jackpot = jackpotWithFixedChance("0.1000");
        when(betRepository.findById("bet-1")).thenReturn(Optional.of(bet()));
        when(jackpotRepository.findById("JP-1")).thenReturn(Optional.of(jackpot));
        when(random.nextDouble()).thenReturn(0.95); // above 10% chance -> loss

        RewardEvaluationResult result = service.evaluate("bet-1");

        assertThat(result.winner()).isFalse();
        assertThat(result.rewardAmount()).isNull();
        assertThat(jackpot.getCurrentPoolValue()).isEqualByComparingTo("2500");
        verify(rewardRepository, never()).save(any());
    }

    @Test
    void alreadyEvaluatedBetReturnsTheRecordedOutcomeWithoutANewDraw() {
        Bet evaluated = bet();
        evaluated.setRewardEvaluated(true);
        when(betRepository.findById("bet-1")).thenReturn(Optional.of(evaluated));
        when(rewardRepository.findByBetId("bet-1")).thenReturn(Optional.of(JackpotReward.builder()
                .betId("bet-1").userId("user-1").jackpotId("JP-1")
                .jackpotRewardAmount(new BigDecimal("2500"))
                .build()));

        RewardEvaluationResult result = service.evaluate("bet-1");

        assertThat(result.winner()).isTrue();
        assertThat(result.alreadyEvaluated()).isTrue();
        assertThat(result.rewardAmount()).isEqualByComparingTo("2500");
        verify(random, never()).nextDouble();
    }

    @Test
    void unknownBetIsRejected() {
        when(betRepository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.evaluate("nope"))
                .isInstanceOf(BetNotFoundException.class);
    }
}
