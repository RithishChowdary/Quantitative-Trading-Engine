package com.rithish.trading.optimizer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import com.rithish.trading.engine.BacktestEngine;
import com.rithish.trading.report.PerformanceMetrics;
import com.rithish.trading.report.PerformanceMetricsCalculator;

/**
 * Compares multiple trading strategies on the same historical
 * market data using multiple performance metrics.
 *
 * <p>The comparison is not based on profit alone.
 * Each strategy is evaluated using metrics such as:
 * total earnings, win rate, maximum drawdown and profit factor.</p>
 *
 * <p>This class is responsible only for comparing strategies.
 * It does not create or optimize strategies.</p>
 */
public class MultiStrategyComparator {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MultiStrategyComparator.class
            );

    private final BacktestEngine backtestEngine;

    private final PerformanceMetricsCalculator metricsCalculator;

    public MultiStrategyComparator(
            BacktestEngine backtestEngine,
            PerformanceMetricsCalculator metricsCalculator) {

        this.backtestEngine =
                Objects.requireNonNull(
                        backtestEngine,
                        "BacktestEngine must not be null"
                );

        this.metricsCalculator =
                Objects.requireNonNull(
                        metricsCalculator,
                        "PerformanceMetricsCalculator must not be null"
                );
    }

    /**
     * Compares the supplied strategies using the same historical
     * market data.
     *
     * @param series historical market data
     * @param strategies strategies to compare
     * @return map containing strategy name and performance result
     */
    public Map<String, StrategyComparisonResult> compare(
            BarSeries series,
            Map<String, Strategy> strategies) {

        Objects.requireNonNull(
                series,
                "BarSeries must not be null"
        );

        Objects.requireNonNull(
                strategies,
                "Strategies must not be null"
        );

        if (series.isEmpty()) {
            throw new IllegalArgumentException(
                    "BarSeries must contain at least one bar"
            );
        }

        if (strategies.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one strategy must be provided"
            );
        }

        log.info(
                "Starting multi-strategy comparison. "
                        + "Series: {}, bars: {}, strategies: {}",
                series.getName(),
                series.getBarCount(),
                strategies.size()
        );

        Map<String, StrategyComparisonResult> results =
                new LinkedHashMap<>();

        for (Map.Entry<String, Strategy> entry
                : strategies.entrySet()) {

            String strategyName =
                    Objects.requireNonNull(
                            entry.getKey(),
                            "Strategy name must not be null"
                    );

            Strategy strategy =
                    Objects.requireNonNull(
                            entry.getValue(),
                            "Strategy must not be null"
                    );

            /*
             * Run the strategy on the same historical data.
             */
            TradingRecord tradingRecord =
                    backtestEngine.run(
                            series,
                            strategy
                    );

            /*
             * Calculate complete performance metrics.
             */
            PerformanceMetrics metrics =
                    metricsCalculator.calculate(
                            series,
                            tradingRecord
                    );

            StrategyComparisonResult result =
                    new StrategyComparisonResult(
                            strategyName,
                            metrics
                    );

            results.put(
                    strategyName,
                    result
            );

            log.info(
                    "Strategy evaluated: {} | "
                            + "Earnings: {} | "
                            + "Win Rate: {} | "
                            + "Drawdown: {} | "
                            + "Profit Factor: {}",
                    strategyName,
                    String.format(
                            "%.2f",
                            metrics.getTotalEarnings()
                    ),
                    String.format(
                            "%.2f%%",
                            metrics.getWinRate() * 100
                    ),
                    String.format(
                            "%.2f",
                            metrics.getMaximumDrawdown()
                    ),
                    String.format(
                            "%.2f",
                            metrics.getProfitFactor()
                    )
            );
        }

        log.info(
                "Multi-strategy comparison completed."
        );

        return results;
    }

    /**
     * Returns the best strategy using a risk-aware
     * comparison instead of profit alone.
     *
     * <p>Comparison priority:</p>
     *
     * <ol>
     *     <li>Higher total earnings</li>
     *     <li>Higher profit factor</li>
     *     <li>Higher win rate</li>
     *     <li>Lower maximum drawdown</li>
     * </ol>
     *
     * @param comparisonResults strategy performance results
     * @return best strategy name
     */
    public String findBestStrategy(
            Map<String, StrategyComparisonResult> comparisonResults) {

        Objects.requireNonNull(
                comparisonResults,
                "Comparison results must not be null"
        );

        if (comparisonResults.isEmpty()) {
            throw new IllegalArgumentException(
                    "Comparison results must not be empty"
            );
        }

        return comparisonResults
                .values()
                .stream()
                .max(
                        (result1, result2) -> {

                            PerformanceMetrics metrics1 =
                                    result1.getPerformanceMetrics();

                            PerformanceMetrics metrics2 =
                                    result2.getPerformanceMetrics();

                            /*
                             * 1. Higher net earnings is preferred.
                             */
                            int earningsComparison =
                                    Double.compare(
                                            metrics1.getTotalEarnings(),
                                            metrics2.getTotalEarnings()
                                    );

                            if (earningsComparison != 0) {
                                return earningsComparison;
                            }

                            /*
                             * 2. Higher profit factor is preferred.
                             */
                            int profitFactorComparison =
                                    Double.compare(
                                            metrics1.getProfitFactor(),
                                            metrics2.getProfitFactor()
                                    );

                            if (profitFactorComparison != 0) {
                                return profitFactorComparison;
                            }

                            /*
                             * 3. Higher win rate is preferred.
                             */
                            int winRateComparison =
                                    Double.compare(
                                            metrics1.getWinRate(),
                                            metrics2.getWinRate()
                                    );

                            if (winRateComparison != 0) {
                                return winRateComparison;
                            }

                            /*
                             * 4. Lower drawdown is preferred.
                             */
                            return Double.compare(
                                    metrics2.getMaximumDrawdown(),
                                    metrics1.getMaximumDrawdown()
                            );
                        }
                )
                .map(
                        StrategyComparisonResult::getStrategyName
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Unable to determine best strategy"
                        )
                );
    }
}