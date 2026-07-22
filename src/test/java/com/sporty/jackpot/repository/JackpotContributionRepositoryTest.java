package com.sporty.jackpot.repository;

import com.sporty.jackpot.domain.Bet;
import com.sporty.jackpot.domain.JackpotContribution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the UNIQUE(bet_id) constraint that makes contribution processing
 * idempotent under Kafka at-least-once delivery (see BetProcessingService).
 */
@SpringBootTest
@ActiveProfiles("mock")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:jackpot-repo-${random.uuid};DB_CLOSE_DELAY=-1")
class JackpotContributionRepositoryTest {

    @Autowired
    private BetRepository betRepository;
    @Autowired
    private JackpotContributionRepository contributionRepository;

    private void persistBet(String id) {
        betRepository.save(Bet.builder()
                .id(id).userId("user-1").jackpotId("JP-FIXED")
                .amount(new BigDecimal("50.00"))
                .build());
    }

    @Test
    void findsAContributionByBetId() {
        persistBet("contrib-bet-1");
        contributionRepository.save(JackpotContribution.builder()
                .betId("contrib-bet-1").userId("user-1").jackpotId("JP-FIXED")
                .stakeAmount(new BigDecimal("50.00"))
                .contributionAmount(new BigDecimal("2.5000"))
                .currentJackpotAmount(new BigDecimal("1002.5000"))
                .build());

        assertThat(contributionRepository.findByBetId("contrib-bet-1")).isPresent();
        assertThat(contributionRepository.findByBetId("no-such-bet")).isEmpty();
    }

    @Test
    void rejectsASecondContributionForTheSameBet() {
        persistBet("contrib-bet-2");
        contributionRepository.saveAndFlush(JackpotContribution.builder()
                .betId("contrib-bet-2").userId("user-1").jackpotId("JP-FIXED")
                .stakeAmount(new BigDecimal("50.00"))
                .contributionAmount(new BigDecimal("2.5000"))
                .currentJackpotAmount(new BigDecimal("1002.5000"))
                .build());

        assertThatThrownBy(() -> contributionRepository.saveAndFlush(JackpotContribution.builder()
                .betId("contrib-bet-2").userId("user-1").jackpotId("JP-FIXED")
                .stakeAmount(new BigDecimal("50.00"))
                .contributionAmount(new BigDecimal("2.5000"))
                .currentJackpotAmount(new BigDecimal("1005.0000"))
                .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
