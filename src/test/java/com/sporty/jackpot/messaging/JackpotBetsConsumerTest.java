package com.sporty.jackpot.messaging;

import com.sporty.jackpot.repository.JackpotContributionRepository;
import com.sporty.jackpot.repository.JackpotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Exercises the real Kafka path against an embedded broker: KafkaBetPublisher
 * serializes a BetMessage as JSON, and JackpotBetsConsumer deserializes the
 * record straight back into a BetMessage (no manual ObjectMapper mapping)
 * before handing it to the processing service. This is the path the "mock"
 * profile integration test bypasses entirely, so it needs its own coverage.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "jackpot-bets", bootstrapServersProperty = "spring.kafka.bootstrap-servers")
// Own in-memory H2 instance so this context doesn't share seeded/mutated data
// with other @SpringBootTest contexts started in the same Surefire JVM.
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:jackpot-kafka-${random.uuid};DB_CLOSE_DELAY=-1")
class JackpotBetsConsumerTest {

    @Autowired
    private KafkaTemplate<String, BetMessage> kafkaTemplate;
    @Autowired
    private JackpotRepository jackpotRepository;
    @Autowired
    private JackpotContributionRepository contributionRepository;

    @Value("${app.kafka.topic}")
    private String topic;

    @Test
    void consumerDeserializesTheRecordDirectlyIntoABetMessage() {
        BetMessage message = new BetMessage("kafka-it-1", "user-7", "JP-FIXED", new BigDecimal("100.00"));

        kafkaTemplate.send(topic, message.jackpotId(), message);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(contributionRepository.findByBetId("kafka-it-1")).isPresent());

        assertThat(jackpotRepository.findById("JP-FIXED").orElseThrow().getCurrentPoolValue())
                .isEqualByComparingTo("1005.0000");
    }
}
