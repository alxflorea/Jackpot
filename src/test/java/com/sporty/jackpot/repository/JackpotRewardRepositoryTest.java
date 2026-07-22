package com.sporty.jackpot.repository;

import com.sporty.jackpot.domain.Bet;
import com.sporty.jackpot.domain.JackpotReward;
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
 * Tests the UNIQUE(bet_id) constraint that guarantees a bet is never paid a
 * jackpot reward twice (see RewardEvaluationService).
 */
@SpringBootTest
@ActiveProfiles("mock")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:jackpot-repo-${random.uuid};DB_CLOSE_DELAY=-1")
class JackpotRewardRepositoryTest {

    @Autowired
    private BetRepository betRepository;
    @Autowired
    private JackpotRewardRepository rewardRepository;

    private void persistBet(String id) {
        betRepository.save(Bet.builder()
                .id(id).userId("user-1").jackpotId("JP-FIXED")
                .amount(new BigDecimal("50.00"))
                .build());
    }

    @Test
    void findsARewardByBetId() {
        persistBet("reward-bet-1");
        rewardRepository.save(JackpotReward.builder()
                .betId("reward-bet-1").userId("user-1").jackpotId("JP-FIXED")
                .jackpotRewardAmount(new BigDecimal("1000.0000"))
                .build());

        assertThat(rewardRepository.findByBetId("reward-bet-1")).isPresent();
        assertThat(rewardRepository.findByBetId("no-such-bet")).isEmpty();
    }

    @Test
    void rejectsASecondRewardForTheSameBet() {
        persistBet("reward-bet-2");
        rewardRepository.saveAndFlush(JackpotReward.builder()
                .betId("reward-bet-2").userId("user-1").jackpotId("JP-FIXED")
                .jackpotRewardAmount(new BigDecimal("1000.0000"))
                .build());

        assertThatThrownBy(() -> rewardRepository.saveAndFlush(JackpotReward.builder()
                .betId("reward-bet-2").userId("user-1").jackpotId("JP-FIXED")
                .jackpotRewardAmount(new BigDecimal("1000.0000"))
                .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
