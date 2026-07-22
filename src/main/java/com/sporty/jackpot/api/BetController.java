package com.sporty.jackpot.api;

import com.sporty.jackpot.api.dto.PublishBetRequest;
import com.sporty.jackpot.api.dto.PublishBetResponse;
import com.sporty.jackpot.api.dto.RewardEvaluationResponse;
import com.sporty.jackpot.messaging.BetMessage;
import com.sporty.jackpot.messaging.BetPublisher;
import com.sporty.jackpot.service.RewardEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bets")
@RequiredArgsConstructor
@Tag(name = "Bets", description = "Publish bets and evaluate them for jackpot rewards")
public class BetController {

    private final BetPublisher betPublisher;
    private final RewardEvaluationService rewardEvaluationService;

    @Operation(summary = "Publish a bet to Kafka",
            description = "Publishes the bet to the jackpot-bets topic. The consumer picks it up "
                    + "and contributes to the matching jackpot pool according to its configuration.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Bet accepted and published"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
    public ResponseEntity<PublishBetResponse> publishBet(@Valid @RequestBody PublishBetRequest request) {
        betPublisher.publish(new BetMessage(
                request.betId(), request.userId(), request.jackpotId(), request.betAmount()));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new PublishBetResponse(request.betId(), "PUBLISHED"));
    }

    @Operation(summary = "Evaluate a bet for the jackpot reward",
            description = "Checks if a contributing bet wins the jackpot reward. Each bet is drawn "
                    + "exactly once; repeated calls return the recorded outcome. On a win the reward "
                    + "is the whole current pool and the pool resets to its initial value.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evaluation outcome"),
            @ApiResponse(responseCode = "404", description = "Bet unknown or not yet processed")
    })
    @PostMapping("/{betId}/reward-evaluation")
    public RewardEvaluationResponse evaluateReward(
            @Parameter(example = "bet-1", description = "Must be a bet that has already been published and contributed")
            @PathVariable String betId) {
        return RewardEvaluationResponse.from(rewardEvaluationService.evaluate(betId));
    }

}
