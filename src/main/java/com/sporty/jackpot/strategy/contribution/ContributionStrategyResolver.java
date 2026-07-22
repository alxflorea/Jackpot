package com.sporty.jackpot.strategy.contribution;

import com.sporty.jackpot.domain.ContributionStrategyType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Resolves the contribution strategy for a config's strategy type. */
@Component
public class ContributionStrategyResolver {

    private final Map<ContributionStrategyType, ContributionStrategy> strategies =
            new EnumMap<>(ContributionStrategyType.class);

    public ContributionStrategyResolver(List<ContributionStrategy> strategyBeans) {
        strategyBeans.forEach(s -> strategies.put(s.type(), s));
    }

    public ContributionStrategy resolve(ContributionStrategyType type) {
        ContributionStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalStateException("No contribution strategy registered for type " + type);
        }
        return strategy;
    }
}
