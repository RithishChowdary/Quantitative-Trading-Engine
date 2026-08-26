package com.rithish.trading.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import com.rithish.trading.config.StrategyConfig;
import com.rithish.trading.model.StrategyType;
import com.rithish.trading.strategy.StrategyRegistry;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StrategyRegistry registry;

    public Strategy getStrategy(
            StrategyType type,
            BarSeries series,
            StrategyConfig config) {

        return registry
                .getFactory(type)
                .create(series, config);
    }
}