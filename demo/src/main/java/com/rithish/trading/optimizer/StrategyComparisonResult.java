package com.rithish.trading.optimizer;

import java.util.Objects;

import com.rithish.trading.report.PerformanceMetrics;

/**
 * Represents the performance result of a single trading strategy.
 *
 * <p>This class keeps the strategy name together with all
 * calculated performance metrics instead of reducing the
 * comparison to profit alone.</p>
 */
public class StrategyComparisonResult {

    private final String strategyName;

    private final PerformanceMetrics performanceMetrics;

    public StrategyComparisonResult(
            String strategyName,
            PerformanceMetrics performanceMetrics) {

        this.strategyName =
                Objects.requireNonNull(
                        strategyName,
                        "Strategy name must not be null"
                );

        this.performanceMetrics =
                Objects.requireNonNull(
                        performanceMetrics,
                        "PerformanceMetrics must not be null"
                );
    }

    public String getStrategyName() {
        return strategyName;
    }

    public PerformanceMetrics getPerformanceMetrics() {
        return performanceMetrics;
    }
}