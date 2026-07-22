
CREATE TABLE jackpot (
    id                      VARCHAR(36) NOT NULL,
    name                    VARCHAR(100) NOT NULL,
    initial_pool_value      DECIMAL(19, 4) NOT NULL,
    current_pool_value      DECIMAL(19, 4) NOT NULL,
    pool_limit              DECIMAL(19, 4),
    contribution_config_id  VARCHAR(50) NOT NULL,
    reward_config_id        VARCHAR(50) NOT NULL,
    version                 BIGINT NOT NULL DEFAULT 0,
    created_at              TIMESTAMP NOT NULL,
    updated_at              TIMESTAMP NOT NULL,
    CONSTRAINT pk_jackpot PRIMARY KEY (id),
    CONSTRAINT fk_jackpot_contrib_cfg FOREIGN KEY (contribution_config_id)
        REFERENCES jackpot_contribution_config (id),
    CONSTRAINT fk_jackpot_reward_cfg FOREIGN KEY (reward_config_id)
        REFERENCES jackpot_reward_config (id)
);
--rollback DROP TABLE jackpot;
