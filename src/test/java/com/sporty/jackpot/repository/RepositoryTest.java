package com.sporty.jackpot.repository;

import com.sporty.jackpot.domain.Bet;
import com.sporty.jackpot.domain.Jackpot;
import com.sporty.jackpot.domain.JackpotContribution;
import com.sporty.jackpot.domain.JackpotReward;
import org.junit.jupiter.api.Nested;
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
 * Repository-layer tests against the real Liquibase-managed schema (H2), covering
 * behaviour beyond what Spring Data generates for free: eager config loading on
 * Jackpot, optimistic-locking version increments, and the unique/foreign key
 * constraints the ledger tables rely on for idempotency.
 */
@SpringBootTest
@ActiveProfiles("mock")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:jackpot-repo-${random.uuid};DB_CLOSE_DELAY=-1")
class RepositoryTest {

    @Autowired
    private JackpotRepository jackpotRepository;
    @Autowired
    private BetRepository betRepository;
    @Autowired
    private JackpotContributionRepository contributionRepository;
    @Autowired
    private JackpotRewardRepository rewardRepository;

    private Bet persistBet(String id) {
        return betRepository.save(Bet.builder()
                .id(id).userId("user-1").jackpotId("JP-FIXED")
                .amount(new BigDecimal("50.00"))
                .build());
    }

    @Nested
    class JackpotRepositoryTests {

        @Test
        void findsASeededJackpotWithItsConfigsEagerlyLoaded() {
            Jackpot jackpot = jackpotRepository.findById("JP-FIXED").orElseThrow();

            assertThat(jackpot.getName()).isEqualTo("Classic Jackpot");
            assertThat(jackpot.getContributionConfig().getId()).isEqualTo("FIXED_5_PERCENT");
            assertThat(jackpot.getRewardConfig().getId()).isEqualTo("FIXED_CHANCE_10_PERCENT");
        }

        @Test
        void returnsEmptyForAnUnknownJackpotId() {
            assertThat(jackpotRepository.findById("does-not-exist")).isEmpty();
        }

        @Test
        void versionIncrementsOnEachSaveForOptimisticLocking() {
            Jackpot jackpot = jackpotRepository.findById("JP-PROGRESSIVE").orElseThrow();
            long versionBeforeSave = jackpot.getVersion();

            jackpot.contribute(new BigDecimal("10.0000"));
            Jackpot saved = jackpotRepository.saveAndFlush(jackpot);

            assertThat(saved.getVersion()).isEqualTo(versionBeforeSave + 1);
        }
    }

    @Nested
    class BetRepositoryTests {

        @Test
        void savesABetAndDefaultsRewardEvaluatedToFalseWithAnAutoTimestamp() {
            Bet saved = persistBet("repo-test-bet-1");

            assertThat(saved.isRewardEvaluated()).isFalse();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(betRepository.existsById("repo-test-bet-1")).isTrue();
        }

        @Test
        void existsByIdIsFalseForAnUnknownBet() {
            assertThat(betRepository.existsById("does-not-exist")).isFalse();
        }
    }

    @Nested
    class JackpotContributionRepositoryTests {

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

    @Nested
    class JackpotRewardRepositoryTests {

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
}
