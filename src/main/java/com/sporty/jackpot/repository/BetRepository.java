package com.sporty.jackpot.repository;

import com.sporty.jackpot.domain.Bet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetRepository extends JpaRepository<Bet, String> {
}
