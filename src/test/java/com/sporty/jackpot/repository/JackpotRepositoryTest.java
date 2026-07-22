package com.sporty.jackpot.repository;

import com.sporty.jackpot.domain.Jackpot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests JackpotRepository against the real Liquibase-managed schema (H2):
 * eager config loading and the @Version optimistic-locking increment, neither
 * of which is exercised by Spring Data's generated CRUD alone.
 */
@SpringBootTest
@ActiveProfiles("mock")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:jackpot-repo-${random.uuid};DB_CLOSE_DELAY=-1")
class JackpotRepositoryTest {

    @Autowired
    private JackpotRepository jackpotRepository;

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
