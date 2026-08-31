package com.rithish.trading.optimizer;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import com.rithish.trading.config.StrategyConfig;

/**
 * Defines the contract for strategy parameter optimization.
 *
 * Implementations are responsible for finding the best
 * strategy configuration for a given historical dataset.
 */
public interface StrategyOptimizer {

    /**
     * Finds the best strategy configuration for the given
     * historical market data.
     *
     * @param series historical market data
     * @return optimized strategy configuration
     */
    StrategyConfig optimize(BarSeries series);

    /**
     * Creates a strategy using the supplied configuration.
     *
     * @param series historical market data
     * @param config strategy configuration
     * @return configured trading strategy
     */
    Strategy createStrategy(
            BarSeries series,
            StrategyConfig config
    );
}