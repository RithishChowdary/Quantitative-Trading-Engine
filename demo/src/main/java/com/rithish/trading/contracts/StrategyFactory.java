package com.rithish.trading.contracts;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import com.rithish.trading.config.StrategyConfig;

public interface StrategyFactory {

    Strategy create(
            BarSeries series,
            StrategyConfig config
    );
}