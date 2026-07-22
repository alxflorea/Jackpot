
CREATE TABLE bet (
    id               VARCHAR(36) NOT NULL,
    user_id          VARCHAR(36) NOT NULL,
    jackpot_id       VARCHAR(36) NOT NULL,
    amount           DECIMAL(19, 4) NOT NULL,
    reward_evaluated BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP NOT NULL,
    CONSTRAINT pk_bet PRIMARY KEY (id),
    CONSTRAINT fk_bet_jackpot FOREIGN KEY (jackpot_id) REFERENCES jackpot (id)
);
--rollback DROP TABLE bet;
