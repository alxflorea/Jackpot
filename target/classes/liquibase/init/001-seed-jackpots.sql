-- Two reusable configs of each kind, and two jackpots wired to them.
-- Percentages/chances are fractions: 0.0500 = 5%.

INSERT INTO jackpot_contribution_config (id, strategy_type, base_percentage, decay_rate, min_percentage) VALUES
    ('FIXED_5_PERCENT', 'FIXED', 0.0500, NULL, NULL),
    ('DECAY_10_TO_1_PERCENT', 'VARIABLE_DECAY', 0.1000, 0.0300, 0.0100);

INSERT INTO jackpot_reward_config (id, strategy_type, base_chance_percentage, growth_rate) VALUES
    ('FIXED_CHANCE_10_PERCENT', 'FIXED', 0.1000, NULL),
    ('GROWING_CHANCE_5_PERCENT_BASE', 'VARIABLE_GROWTH', 0.0500, 0.2000);

INSERT INTO jackpot (id, name, initial_pool_value, current_pool_value, pool_limit,
                     contribution_config_id, reward_config_id, version, created_at, updated_at) VALUES
    ('JP-FIXED', 'Classic Jackpot', 1000.0000, 1000.0000, NULL,
     'FIXED_5_PERCENT', 'FIXED_CHANCE_10_PERCENT', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JP-PROGRESSIVE', 'Progressive Jackpot', 5000.0000, 5000.0000, 10000.0000,
     'DECAY_10_TO_1_PERCENT', 'GROWING_CHANCE_5_PERCENT_BASE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
--rollback DELETE FROM jackpot WHERE id IN ('JP-FIXED', 'JP-PROGRESSIVE');
--rollback DELETE FROM jackpot_reward_config WHERE id IN ('FIXED_CHANCE_10_PERCENT', 'GROWING_CHANCE_5_PERCENT_BASE');
--rollback DELETE FROM jackpot_contribution_config WHERE id IN ('FIXED_5_PERCENT', 'DECAY_10_TO_1_PERCENT');
