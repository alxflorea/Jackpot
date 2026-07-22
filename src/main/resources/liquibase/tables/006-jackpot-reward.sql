
CREATE TABLE jackpot_reward (
    id                     VARCHAR(36) NOT NULL,
    bet_id                 VARCHAR(36) NOT NULL,
    user_id                VARCHAR(36) NOT NULL,
    jackpot_id             VARCHAR(36) NOT NULL,
    jackpot_reward_amount  DECIMAL(19, 4) NOT NULL,
    created_at             TIMESTAMP NOT NULL,
    CONSTRAINT pk_jackpot_reward PRIMARY KEY (id),
    CONSTRAINT uq_reward_bet_id UNIQUE (bet_id),
    CONSTRAINT fk_reward_bet FOREIGN KEY (bet_id) REFERENCES bet (id),
    CONSTRAINT fk_reward_jackpot FOREIGN KEY (jackpot_id) REFERENCES jackpot (id)
);
--rollback DROP TABLE jackpot_reward;
