package com.rithish.trading.optimizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.pnl.ProfitCriterion;

import com.rithish.trading.config.StrategyConfig;
import com.rithish.trading.engine.BacktestEngine;

/**
 * Performs walk-forward optimization to evaluate a trading strategy
 * on unseen historical data.
 *
 * <p>For every walk-forward window:</p>
 *
 * <ol>
 *     <li>Use the training window to find the best parameters.</li>
 *     <li>Create a strategy using those parameters.</li>
 *     <li>Evaluate the strategy on the following unseen testing window.</li>
 *     <li>Move the window forward and repeat.</li>
 * </ol>
 *
 * <p>This helps reduce the risk of evaluating a strategy only on
 * data that was used to optimize its parameters.</p>
 */
public class WalkForwardOptimizer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    WalkForwardOptimizer.class
            );

    private final ParameterOptimizer parameterOptimizer;
    private final BacktestEngine backtestEngine;

    private final int trainingWindowSize;
    private final int testingWindowSize;
    private final int stepSize;

    public WalkForwardOptimizer(
            ParameterOptimizer parameterOptimizer,
            BacktestEngine backtestEngine,
            int trainingWindowSize,
            int testingWindowSize,
            int stepSize) {

        this.parameterOptimizer =
                Objects.requireNonNull(
                        parameterOptimizer,
                        "ParameterOptimizer must not be null"
                );

        this.backtestEngine =
                Objects.requireNonNull(
                        backtestEngine,
                        "BacktestEngine must not be null"
                );

        if (trainingWindowSize <= 0) {
            throw new IllegalArgumentException(
                    "Training window size must be greater than zero"
            );
        }

        if (testingWindowSize <= 0) {
            throw new IllegalArgumentException(
                    "Testing window size must be greater than zero"
            );
        }

        if (stepSize <= 0) {
            throw new IllegalArgumentException(
                    "Step size must be greater than zero"
            );
        }

        this.trainingWindowSize = trainingWindowSize;
        this.testingWindowSize = testingWindowSize;
        this.stepSize = stepSize;
    }

    /**
     * Executes walk-forward optimization.
     *
     * @param series complete historical market data
     * @return immutable list containing out-of-sample results
     */
    public List<WalkForwardResult> optimize(
            BarSeries series) {

        Objects.requireNonNull(
                series,
                "BarSeries must not be null"
        );

        if (series.isEmpty()) {
            throw new IllegalArgumentException(
                    "BarSeries must contain at least one bar"
            );
        }

        int requiredBars =
                trainingWindowSize + testingWindowSize;

        if (series.getBarCount() < requiredBars) {

            throw new IllegalArgumentException(
                    "Not enough bars for walk-forward optimization. "
                            + "Required at least "
                            + requiredBars
                            + ", found "
                            + series.getBarCount()
            );
        }

        log.info(
                "Starting walk-forward optimization. "
                        + "Series: {}, bars: {}, training: {}, "
                        + "testing: {}, step: {}",
                series.getName(),
                series.getBarCount(),
                trainingWindowSize,
                testingWindowSize,
                stepSize
        );

        List<WalkForwardResult> results =
                new ArrayList<>();

        int windowNumber = 1;

        /*
         * Example with:
         *
         * training = 400
         * testing  = 100
         * step     = 100
         *
         * Window 1:
         * Training: 0   -> 400
         * Testing : 400 -> 500
         *
         * Window 2:
         * Training: 100 -> 500
         * Testing : 500 -> 600
         *
         * Window 3:
         * Training: 200 -> 600
         * Testing : 600 -> 700
         */
        for (
                int trainingStart = 0;
                trainingStart
                        + trainingWindowSize
                        + testingWindowSize
                        <= series.getBarCount();
                trainingStart += stepSize
        ) {

            int trainingEnd =
                    trainingStart + trainingWindowSize;

            int testingStart =
                    trainingEnd;

            int testingEnd =
                    testingStart + testingWindowSize;

            log.info(
                    "Walk-forward window {}: "
                            + "training [{}-{}), "
                            + "testing [{}-{})",
                    windowNumber,
                    trainingStart,
                    trainingEnd,
                    testingStart,
                    testingEnd
            );

            /*
             * Create independent training and testing
             * BarSeries objects.
             */
            BarSeries trainingSeries =
                    series.getSubSeries(
                            trainingStart,
                            trainingEnd
                    );

            BarSeries testingSeries =
                    series.getSubSeries(
                            testingStart,
                            testingEnd
                    );

            /*
             * IMPORTANT:
             *
             * Optimization happens ONLY on training data.
             */
            StrategyConfig bestConfig =
                    parameterOptimizer.optimize(
                            trainingSeries
                    );

            log.info(
                    "Window {} best configuration: "
                            + "EMA {}/{} | RSI {} | "
                            + "Buy {} | Sell {}",
                    windowNumber,
                    bestConfig.getFastEmaPeriod(),
                    bestConfig.getSlowEmaPeriod(),
                    bestConfig.getRsiPeriod(),
                    bestConfig.getRsiBuyThreshold(),
                    bestConfig.getRsiSellThreshold()
            );

            /*
             * Create the strategy against the TESTING
             * series, not the training series.
             *
             * This is important because TA4J indicators
             * belong to their associated BarSeries.
             */
            Strategy testingStrategy =
                    parameterOptimizer.createStrategy(
                            testingSeries,
                            bestConfig
                    );

            /*
             * Run the optimized configuration against
             * completely unseen testing data.
             */
            TradingRecord testingRecord =
                    backtestEngine.run(
                            testingSeries,
                            testingStrategy
                    );

            double testingProfit =
                    new ProfitCriterion()
                            .calculate(
                                    testingSeries,
                                    testingRecord
                            )
                            .doubleValue();

            WalkForwardResult result =
                    new WalkForwardResult(
                            windowNumber,
                            trainingStart,
                            trainingEnd,
                            testingStart,
                            testingEnd,
                            bestConfig,
                            testingProfit,
                            testingRecord.getPositionCount()
                    );

            results.add(result);

            log.info(
                    "Window {} completed. "
                            + "Out-of-sample profit: {}",
                    windowNumber,
                    String.format(
                            "%.2f",
                            testingProfit
                    )
            );

            windowNumber++;
        }

        log.info(
                "Walk-forward optimization completed. "
                        + "Windows evaluated: {}",
                results.size()
        );

        return Collections.unmodifiableList(
                results
        );
    }

    /**
     * Calculates the total out-of-sample profit across
     * all walk-forward testing windows.
     *
     * @param results walk-forward results
     * @return total out-of-sample profit
     */
    public double calculateTotalProfit(
            List<WalkForwardResult> results) {

        Objects.requireNonNull(
                results,
                "Walk-forward results must not be null"
        );

        return results.stream()
                .mapToDouble(
                        WalkForwardResult::getTestingProfit
                )
                .sum();
    }

    /**
     * Represents the result of one walk-forward window.
     */
    public static final class WalkForwardResult {

        private final int windowNumber;

        private final int trainingStart;
        private final int trainingEnd;

        private final int testingStart;
        private final int testingEnd;

        private final StrategyConfig bestConfig;

        private final double testingProfit;

        private final int completedTrades;

        public WalkForwardResult(
                int windowNumber,
                int trainingStart,
                int trainingEnd,
                int testingStart,
                int testingEnd,
                StrategyConfig bestConfig,
                double testingProfit,
                int completedTrades) {

            this.windowNumber = windowNumber;
            this.trainingStart = trainingStart;
            this.trainingEnd = trainingEnd;
            this.testingStart = testingStart;
            this.testingEnd = testingEnd;

            this.bestConfig =
                    Objects.requireNonNull(
                            bestConfig,
                            "Best configuration must not be null"
                    );

            this.testingProfit = testingProfit;
            this.completedTrades = completedTrades;
        }

        public int getWindowNumber() {
            return windowNumber;
        }

        public int getTrainingStart() {
            return trainingStart;
        }

        public int getTrainingEnd() {
            return trainingEnd;
        }

        public int getTestingStart() {
            return testingStart;
        }

        public int getTestingEnd() {
            return testingEnd;
        }

        public StrategyConfig getBestConfig() {
            return bestConfig;
        }

        public double getTestingProfit() {
            return testingProfit;
        }

        public int getCompletedTrades() {
            return completedTrades;
        }
    }
}