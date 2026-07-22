package com.sporty.jackpot;

import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end flow on the mock profile: publish endpoint -> (mocked) Kafka ->
 * contribution -> reward evaluation endpoint. No broker needed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
// Own in-memory H2 instance so this context doesn't share seeded/mutated data
// with other @SpringBootTest contexts started in the same Surefire JVM.
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:jackpot-mock-${random.uuid};DB_CLOSE_DELAY=-1")
class JackpotFlowIntegrationTest {

    @TestConfiguration
    static class DeterministicRandomConfig {
        @Bean
        @Primary
        RandomGenerator deterministicRandom() {
            return new RandomGenerator() {
                @Override
                public long nextLong() {
                    return Long.MAX_VALUE; // nextDouble() ~ 1.0 -> never wins
                }

                @Override
                public double nextDouble() {
                    return 0.9999;
                }
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JackpotRepository jackpotRepository;
    @Autowired
    private JackpotContributionRepository contributionRepository;

    @Test
    void betIsPublishedContributedAndEvaluated() throws Exception {
        mockMvc.perform(post("/api/v1/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "betId": "it-bet-1",
                                  "userId": "user-42",
                                  "jackpotId": "JP-FIXED",
                                  "betAmount": 100.00
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.betId").value("it-bet-1"));

        // 5% of 100 contributed on top of the seeded 1000 pool
        assertThat(jackpotRepository.findById("JP-FIXED").orElseThrow().getCurrentPoolValue())
                .isEqualByComparingTo("1005.0000");
        assertThat(contributionRepository.findByBetId("it-bet-1")).isPresent();

        mockMvc.perform(post("/api/v1/bets/it-bet-1/reward-evaluation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.betId").value("it-bet-1"))
                .andExpect(jsonPath("$.winner").value(false));

        // Second evaluation returns the recorded outcome, no re-draw
        mockMvc.perform(post("/api/v1/bets/it-bet-1/reward-evaluation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alreadyEvaluated").value(true));
    }

    @Test
    void evaluatingAnUnknownBetReturns404() throws Exception {
        mockMvc.perform(post("/api/v1/bets/does-not-exist/reward-evaluation"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidPublishRequestReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"betId\": \"x\"}"))
                .andExpect(status().isBadRequest());
    }
}
