package com.sporty.jackpot.service;

import com.sporty.jackpot.domain.ContributionStrategyType;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotContribution;
import com.sporty.jackpot.domain.JackpotContributionConfig;
import com.sporty.jackpot.messaging.BetMessage;
import com.sporty.jackpot.repository.BetRepository;
import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import com.sporty.jackpot.strategy.contribution.ContributionStrategyResolver;
import com.sporty.jackpot.strategy.contribution.FixedContributionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetProcessingServiceTest {

    @Mock
    private BetRepository betRepository;
    @Mock
    private JackpotRepository jackpotRepository;
    @Mock
    private JackpotContributionRepository contributionRepository;

    private BetProcessingService service;

    private final BetMessage message = new BetMessage(
            "bet-1", "user-1", "JP-1", new BigDecimal("100.00"));

    @BeforeEach
    void setUp() {
        service = new BetProcessingService(
                betRepository, jackpotRepository, contributionRepository,
                new ContributionStrategyResolver(List.of(new FixedContributionStrategy())));
    }

    private Jackpot fixedJackpot() {
        return Jackpot.builder()
                .id("JP-1")
                .initialPoolValue(new BigDecimal("1000"))
                .currentPoolValue(new BigDecimal("1000"))
                .contributionConfig(JackpotContributionConfig.builder()
                        .strategyType(ContributionStrategyType.FIXED)
                        .basePercentage(new BigDecimal("0.0500"))
                        .build())
                .build();
    }

    @Test
    void contributesToThePoolAndRecordsTheContribution() {
        Jackpot jackpot = fixedJackpot();
        when(betRepository.existsById("bet-1")).thenReturn(false);
        when(jackpotRepository.findById("JP-1")).thenReturn(Optional.of(jackpot));

        service.processBet(message);

        assertThat(jackpot.getCurrentPoolValue()).isEqualByComparingTo("1005.0000");

        ArgumentCaptor<JackpotContribution> captor = ArgumentCaptor.forClass(JackpotContribution.class);
        verify(contributionRepository).save(captor.capture());
        JackpotContribution contribution = captor.getValue();
        assertThat(contribution.getBetId()).isEqualTo("bet-1");
        assertThat(contribution.getStakeAmount()).isEqualByComparingTo("100.00");
        assertThat(contribution.getContributionAmount()).isEqualByComparingTo("5.0000");
        assertThat(contribution.getCurrentJackpotAmount()).isEqualByComparingTo("1005.0000");
    }

    @Test
    void skipsBetsThatWereAlreadyProcessed() {
        when(betRepository.existsById("bet-1")).thenReturn(true);

        service.processBet(message);

        verify(jackpotRepository, never()).findById(any());
        verify(contributionRepository, never()).save(any());
    }

    @Test
    void ignoresBetsWithoutAMatchingJackpot() {
        when(betRepository.existsById("bet-1")).thenReturn(false);
        when(jackpotRepository.findById("JP-1")).thenReturn(Optional.empty());

        service.processBet(message);

        verify(betRepository, never()).save(any());
        verify(contributionRepository, never()).save(any());
    }
}
