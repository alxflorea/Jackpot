# Jackpot Service

Backend service that receives bets, contributes them to jackpot pools and evaluates them for jackpot rewards.

**Stack:** Java 21, Spring Boot 3.5, Spring Data JPA, Liquibase, H2 (in-memory), Spring Kafka, Maven.

## How to run

Uses the bundled Maven Wrapper (`./mvnw`, or `mvnw.cmd` on Windows), so no local Maven install is
required — it downloads a pinned, known-good Maven version on first run. Replace `./mvnw` with
`mvnw.cmd` in the commands below if you're on Windows. On PowerShell, quote `-D` arguments as shown
below — unquoted, PowerShell can mis-parse them before they ever reach Maven.

### Option A — with Kafka (default profile)

Requires Docker.

```bash
docker compose up -d          # starts a single-node Kafka on localhost:9092
./mvnw spring-boot:run
```

### Option B — without Kafka (mock profile)

As allowed by the assignment, the Kafka producer is mocked: it logs the payload and hands the bet
directly to the processing service (simulating an immediate consume). No broker needed.

```bash
./mvnw spring-boot:run "-Dspring-boot.run.profiles=mock"
```

The app starts on `http://localhost:8080`.

- **Swagger UI:** `http://localhost:8080/swagger-ui.html` (OpenAPI spec at `/v3/api-docs`)
- **H2 console:** `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:jackpot`, user `sa`, empty password)

### Tests

```bash
./mvnw test
```

Includes unit tests for the contribution/reward strategies and services, an end-to-end
integration test (publish → consume → contribute → evaluate) on the mock profile, and a test
against an embedded Kafka broker that exercises the real publish/consume path (see below).

## API

### 1. Publish a bet to Kafka

```bash
curl -X POST http://localhost:8080/api/v1/bets \
  -H "Content-Type: application/json" \
  -d '{"betId":"bet-1","userId":"user-1","jackpotId":"JP-FIXED","betAmount":100.00}'
```

Returns `202 Accepted`. The bet is published to the `jackpot-bets` topic; the consumer picks it
up and applies the jackpot contribution.

### 2. Evaluate a bet for the jackpot reward

```bash
curl -X POST http://localhost:8080/api/v1/bets/bet-1/reward-evaluation
```

Response:

```json
{"betId":"bet-1","jackpotId":"JP-FIXED","winner":false,"rewardAmount":null,"alreadyEvaluated":false}
```

On a win, `rewardAmount` is the whole current pool and the pool resets to its initial value.
Returns `404` if the bet is unknown (not yet consumed/contributed).

## Seeded jackpots

| Jackpot ID | Contribution | Reward |
|---|---|---|
| `JP-FIXED` | fixed 5% of the bet | fixed 10% chance |
| `JP-PROGRESSIVE` | starts at 10%, decays as the pool grows (floor 1%) | starts at 5%, grows with the pool, 100% at the 10,000 pool limit |

Percentages are stored as fractions (`0.0500` = 5%). Seed data is loaded by the last Liquibase
changeset, see below.

## Database schema

Schema is owned by Liquibase, not by Hibernate. The changelog lives under
[liquibase](src/main/resources/liquibase).

```
jackpot_contribution_config ──┐
                              ├──< jackpot >── bet ──< jackpot_contribution
jackpot_reward_config ────────┘          └────────────< jackpot_reward
```

`jackpot_contribution` and `jackpot_reward` are the audit/ledger tables required by the
assignment, storing exactly the fields it lists (including the pool snapshot after each
contribution).

## Design notes

- **Strategy pattern for extensibility.** `ContributionStrategy` and `RewardStrategy` are Spring
  beans keyed by strategy type; a new behaviour is one new class, no changes to existing code.
  Configs (`jackpot_contribution_config`, `jackpot_reward_config`) are reusable rows referenced
  by jackpots via FK, so many jackpots can share one configuration.
- **Variable formulas.** Both use `poolGrowthFactor = (currentPool − initialPool) / initialPool`:
  - decay contribution: `max(minPercentage, base − decayRate × growthFactor)`
  - growth reward chance: `min(1, base + growthRate × growthFactor)`, forced to `1` once the
    pool hits its limit.
- **Idempotency.** Kafka is at-least-once, so a redelivered bet is skipped (`bet` PK +
  `UNIQUE(bet_id)` on the contribution ledger). A bet is reward-evaluated exactly once: repeated
  calls return the recorded outcome instead of drawing again, so the endpoint cannot be farmed
  for extra win chances; `UNIQUE(bet_id)` on `jackpot_reward` guarantees a bet is never paid twice.
- **A jackpot is one shared pot, not one per bet.** Win chance and payout are always computed
  from the jackpot's *live* pool value at evaluation time, not from the pool as it stood when a
  given bet contributed. This mirrors a real progressive jackpot — whoever's check lands first
  takes whatever is in the pot right then, and the pot resets for everyone. See
  [Trade-offs / next steps](#trade-offs--next-steps) below for why this is more than a stylistic
  choice.
- **Concurrency.** The jackpot row carries a `@Version` field (optimistic locking), so two
  concurrent writes to the same jackpot (two contributions, or two reward evaluations racing on
  the same jackpot) cannot silently lose an update. Known gap: the losing writer's
  `ObjectOptimisticLockingFailureException` isn't caught anywhere yet, so it currently surfaces as
  a raw `500` instead of a clean `409 Conflict` with a retry hint.
- **Money as `BigDecimal`** (`DECIMAL(19,4)`), never floating point.
- **Randomness** is injected as a `RandomGenerator` bean (`SecureRandom` in production),
  swapped for a deterministic stub in tests.

### Trade-offs / next steps

1. **Move reward evaluation to be eager** — drawn immediately after contribution, inside
   `BetProcessingService`, instead of on-demand via the separate endpoint. Right now win chance
   and payout are computed from the jackpot's *live* pool at evaluation time, not from the pool
   as it stood when the bet contributed — so a bet can be judged against a pool other, unrelated
   bets have already reset, purely because of *when* someone happened to call the endpoint.
   Evaluating eagerly would fix this for free: evaluation order becomes identical to contribution
   order (both driven by Kafka's per-partition ordering), no bet is ever "forgotten," and the
   optimistic-locking race between concurrent evaluations disappears too.
   [RewardEvaluationOrderingTest](src/test/java/com/sporty/jackpot/service/RewardEvaluationOrderingTest.java)
   demonstrates the concrete consequence. Kept as specified here since the assignment enumerates
   reward evaluation as its own use case with a dedicated endpoint — this is the change I'd
   prioritize first in a real system.
2. Map `ObjectOptimisticLockingFailureException` to a `409 Conflict` (see Concurrency above).

Otherwise kept within the assignment's ~90-minute scope; in a production system I would also add:
retry with backoff + dead-letter topic for the consumer, transactional outbox for the publish
endpoint, pagination/admin endpoints for jackpots, and metrics on pool sizes and win rates.
