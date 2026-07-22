
CREATE TABLE jackpot_reward_config (
    id                     VARCHAR(50) NOT NULL, -- natural key, e.g. 'FIXED_CHANCE_10_PERCENT'
    strategy_type          VARCHAR(50) NOT NULL,
    base_chance_percentage DECIMAL(5, 4) NOT NULL,
    growth_rate            DECIMAL(5, 4),
    CONSTRAINT pk_jackpot_reward_config PRIMARY KEY (id),
    CONSTRAINT chk_reward_cfg_params CHECK (
        strategy_type <> 'VARIABLE_GROWTH' OR growth_rate IS NOT NULL
    ),
    CONSTRAINT chk_reward_cfg_strategy_type CHECK (strategy_type IN ('FIXED', 'VARIABLE_GROWTH'))
);
--rollback DROP TABLE jackpot_reward_config;
