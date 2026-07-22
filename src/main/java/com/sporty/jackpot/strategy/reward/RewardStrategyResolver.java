package com.sporty.jackpot.strategy.reward;

import com.sporty.jackpot.domain.RewardStrategyType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Resolves the reward strategy for a config's strategy type. */
@Component
public class RewardStrategyResolver {

    private final Map<RewardStrategyType, RewardStrategy> strategies =
            new EnumMap<>(RewardStrategyType.class);

    public RewardStrategyResolver(List<RewardStrategy> strategyBeans) {
        strategyBeans.forEach(s -> strategies.put(s.type(), s));
    }

    public RewardStrategy resolve(RewardStrategyType type) {
        RewardStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalStateException("No reward strategy registered for type " + type);
        }
        return strategy;
    }
}
