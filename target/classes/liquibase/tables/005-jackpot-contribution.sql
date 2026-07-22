
CREATE TABLE jackpot_contribution (
    id                      VARCHAR(36) NOT NULL,
    bet_id                  VARCHAR(36) NOT NULL,
    user_id                 VARCHAR(36) NOT NULL,
    jackpot_id              VARCHAR(36) NOT NULL,
    stake_amount            DECIMAL(19, 4) NOT NULL,
    contribution_amount     DECIMAL(19, 4) NOT NULL,
    current_jackpot_amount  DECIMAL(19, 4) NOT NULL,
    created_at              TIMESTAMP NOT NULL,
    CONSTRAINT pk_jackpot_contribution PRIMARY KEY (id),
    CONSTRAINT uq_contribution_bet_id UNIQUE (bet_id),
    CONSTRAINT fk_contribution_bet FOREIGN KEY (bet_id) REFERENCES bet (id),
    CONSTRAINT fk_contribution_jackpot FOREIGN KEY (jackpot_id) REFERENCES jackpot (id)
);
--rollback DROP TABLE jackpot_contribution;
