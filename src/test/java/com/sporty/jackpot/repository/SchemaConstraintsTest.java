package com.sporty.jackpot.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Confirms the database itself rejects a strategy_type value the application
 * doesn't know how to resolve, instead of silently accepting bad data that
 * would only fail later, on the hot path, when that jackpot's config is
 * loaded (see ContributionStrategyResolver / RewardStrategyResolver).
 */
@SpringBootTest
@ActiveProfiles("mock")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:jackpot-constraints-${random.uuid};DB_CLOSE_DELAY=-1")
class SchemaConstraintsTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void rejectsAnUnknownContributionStrategyType() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO jackpot_contribution_config (id, strategy_type, base_percentage) "
                        + "VALUES ('BAD_CONFIG', 'NOT_A_REAL_STRATEGY', 0.05)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsAnUnknownRewardStrategyType() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO jackpot_reward_config (id, strategy_type, base_chance_percentage) "
                        + "VALUES ('BAD_CONFIG_2', 'NOT_A_REAL_STRATEGY', 0.05)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
