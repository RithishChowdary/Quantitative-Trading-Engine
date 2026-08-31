package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import com.rithish.trading.config.StrategyConfig;
import com.rithish.trading.engine.BacktestEngine;
import com.rithish.trading.indicator.IndicatorRegistry;
import com.rithish.trading.optimizer.MultiStrategyComparator;
import com.rithish.trading.optimizer.StrategyComparisonResult;
import com.rithish.trading.report.PerformanceMetricsCalculator;
import com.rithish.trading.loader.CsvMarketDataLoader;
import com.rithish.trading.strategy.EmaRsiStrategyFactory;

class MultiStrategyComparatorTest {

    private final BacktestEngine backtestEngine =
            new BacktestEngine();

    private final PerformanceMetricsCalculator metricsCalculator =
            new PerformanceMetricsCalculator();

    private final MultiStrategyComparator comparator =
            new MultiStrategyComparator(
                    backtestEngine,
                    metricsCalculator
            );

    @Test
    void shouldCompareMultipleStrategies() {

        CsvMarketDataLoader loader =
                new CsvMarketDataLoader();

        BarSeries series =
                loader.loadSeries("TCS");

        StrategyConfig config1 =
                new StrategyConfig(
                        9,
                        21,
                        14,
                        55.0,
                        45.0
                );

        StrategyConfig config2 =
                new StrategyConfig(
                        10,
                        26,
                        14,
                        60.0,
                        40.0
                );

        IndicatorRegistry indicatorRegistry =
                new IndicatorRegistry();

        EmaRsiStrategyFactory factory =
                new EmaRsiStrategyFactory(
                        indicatorRegistry
                );

        Strategy strategy1 =
                factory.create(
                        series,
                        config1
                );

        Strategy strategy2 =
                factory.create(
                        series,
                        config2
                );

        Map<String, Strategy> strategies =
                new LinkedHashMap<>();

        strategies.put(
                "Strategy-1",
                strategy1
        );

        strategies.put(
                "Strategy-2",
                strategy2
        );

        Map<String, StrategyComparisonResult> results =
                comparator.compare(
                        series,
                        strategies
                );

        assertEquals(
                2,
                results.size()
        );

        assertFalse(
                results.isEmpty()
        );

        assertEquals(
                true,
                results.containsKey("Strategy-1")
        );

        assertEquals(
                true,
                results.containsKey("Strategy-2")
        );

        assertNotNull(
                results.get("Strategy-1")
        );

        assertNotNull(
                results.get("Strategy-2")
        );

        assertNotNull(
                results.get("Strategy-1")
                        .getPerformanceMetrics()
        );

        assertNotNull(
                results.get("Strategy-2")
                        .getPerformanceMetrics()
        );
    }

    @Test
    void shouldFindBestStrategyUsingPerformanceMetrics() {

        PerformanceMetricsCalculator calculator =
                new PerformanceMetricsCalculator();

        /*
         * Strategy 1:
         * Higher earnings but worse profit factor.
         */
        com.rithish.trading.report.PerformanceMetrics metrics1 =
                new com.rithish.trading.report.PerformanceMetrics(
                        10,
                        6,
                        4,
                        0.60,
                        500.0,
                        200.0,
                        300.0,
                        30.0,
                        0.20,
                        2.50
                );

        /*
         * Strategy 2:
         * Lower earnings but better profit factor.
         *
         * Earnings remain the first priority in the
         * current comparison policy.
         */
        com.rithish.trading.report.PerformanceMetrics metrics2 =
                new com.rithish.trading.report.PerformanceMetrics(
                        10,
                        8,
                        2,
                        0.80,
                        450.0,
                        100.0,
                        350.0,
                        35.0,
                        0.10,
                        4.50
                );

        Map<String, StrategyComparisonResult> results =
                new LinkedHashMap<>();

        results.put(
                "Strategy-1",
                new StrategyComparisonResult(
                        "Strategy-1",
                        metrics1
                )
        );

        results.put(
                "Strategy-2",
                new StrategyComparisonResult(
                        "Strategy-2",
                        metrics2
                )
        );

        String best =
                comparator.findBestStrategy(
                        results
                );

        assertEquals(
                "Strategy-2",
                best
        );
    }

    @Test
    void shouldRejectEmptyStrategies() {

        CsvMarketDataLoader loader =
                new CsvMarketDataLoader();

        BarSeries series =
                loader.loadSeries("TCS");

        Map<String, Strategy> strategies =
                new LinkedHashMap<>();

        assertThrows(
                IllegalArgumentException.class,
                () -> comparator.compare(
                        series,
                        strategies
                )
        );
    }

    @Test
    void shouldRejectEmptyComparisonResults() {

        Map<String, StrategyComparisonResult> results =
                new LinkedHashMap<>();

        assertThrows(
                IllegalArgumentException.class,
                () -> comparator.findBestStrategy(
                        results
                )
        );
    }
}