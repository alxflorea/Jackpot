package com.sporty.jackpot.repository;

import com.sporty.jackpot.domain.Bet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("mock")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:jackpot-repo-${random.uuid};DB_CLOSE_DELAY=-1")
class BetRepositoryTest {

    @Autowired
    private BetRepository betRepository;

    @Test
    void savesABetAndDefaultsRewardEvaluatedToFalseWithAnAutoTimestamp() {
        Bet saved = betRepository.save(Bet.builder()
                .id("repo-test-bet-1").userId("user-1").jackpotId("JP-FIXED")
                .amount(new BigDecimal("25.00"))
                .build());

        assertThat(saved.isRewardEvaluated()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(betRepository.existsById("repo-test-bet-1")).isTrue();
    }

    @Test
    void existsByIdIsFalseForAnUnknownBet() {
        assertThat(betRepository.existsById("does-not-exist")).isFalse();
    }
}
