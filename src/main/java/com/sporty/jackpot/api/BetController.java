package com.sporty.jackpot.api;

import com.sporty.jackpot.api.dto.PublishBetRequest;
import com.sporty.jackpot.api.dto.PublishBetResponse;
import com.sporty.jackpot.messaging.BetMessage;
import com.sporty.jackpot.messaging.BetPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bets")
@RequiredArgsConstructor
public class BetController {

    private final BetPublisher betPublisher;

    @PostMapping
    public ResponseEntity<PublishBetResponse> publishBet(@Valid @RequestBody PublishBetRequest request) {
        betPublisher.publish(new BetMessage(
                request.betId(), request.userId(), request.jackpotId(), request.betAmount()));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new PublishBetResponse(request.betId(), "PUBLISHED"));
    }


}
