package com.sporty.jackpot.service;

public class BetNotFoundException extends RuntimeException {

    public BetNotFoundException(String betId) {
        super("Bet " + betId + " is not a contributing bet (unknown or not yet processed)");
    }
}
