
CREATE TABLE jackpot_contribution_config (
    id              VARCHAR(50) NOT NULL, -- natural key, e.g. 'FIXED_5_PERCENT'
    strategy_type   VARCHAR(50) NOT NULL,
    base_percentage DECIMAL(5, 4) NOT NULL,
    decay_rate      DECIMAL(5, 4),
    min_percentage  DECIMAL(5, 4),
    CONSTRAINT pk_jackpot_contribution_config PRIMARY KEY (id),
    CONSTRAINT chk_contrib_cfg_params CHECK (
        strategy_type <> 'VARIABLE_DECAY' OR decay_rate IS NOT NULL
    ),
    CONSTRAINT chk_contrib_cfg_strategy_type CHECK (strategy_type IN ('FIXED', 'VARIABLE_DECAY'))
);
--rollback DROP TABLE jackpot_contribution_config;
