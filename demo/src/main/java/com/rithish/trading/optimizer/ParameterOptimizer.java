package com.rithish.trading.optimizer;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.pnl.ProfitCriterion;

import com.rithish.trading.config.StrategyConfig;
import com.rithish.trading.engine.BacktestEngine;
import com.rithish.trading.model.StrategyType;
import com.rithish.trading.service.impl.StrategyService;

/**
 * Searches different strategy parameter combinations and
 * returns the configuration that produces the highest profit.
 *
 * <p>The optimizer currently evaluates the EMA + RSI strategy.</p>
 */
@Component
public class ParameterOptimizer implements StrategyOptimizer {

    private static final Logger log =
            LoggerFactory.getLogger(ParameterOptimizer.class);

    private final StrategyService strategyService;
    private final BacktestEngine backtestEngine;

    public ParameterOptimizer(
            StrategyService strategyService,
            BacktestEngine backtestEngine) {

        this.strategyService =
                Objects.requireNonNull(
                        strategyService,
                        "StrategyService must not be null"
                );

        this.backtestEngine =
                Objects.requireNonNull(
                        backtestEngine,
                        "BacktestEngine must not be null"
                );
    }

    /**
     * Finds the best EMA + RSI parameter combination.
     *
     * <p>The current search space is intentionally small so that
     * optimization remains easy to understand and fast to execute.</p>
     */
    @Override
    public StrategyConfig optimize(BarSeries series) {

        Objects.requireNonNull(
                series,
                "BarSeries must not be null"
        );

        if (series.isEmpty()) {
            throw new IllegalArgumentException(
                    "BarSeries must contain at least one bar"
            );
        }

        log.info(
                "Starting parameter optimization. Series: {}, bars: {}",
                series.getName(),
                series.getBarCount()
        );

        StrategyConfig bestConfig = null;
        double bestProfit = Double.NEGATIVE_INFINITY;

        /*
         * Search space
         *
         * Fast EMA : 9, 10, 12
         * Slow EMA : 20, 21, 26
         * RSI      : 14
         * Buy      : 50, 55, 60
         * Sell     : 40, 45, 50
         */
        int[] fastEmaPeriods = {
                9, 10, 12
        };

        int[] slowEmaPeriods = {
                20, 21, 26
        };

        int[] rsiPeriods = {
                14
        };

        double[] buyThresholds = {
                50.0, 55.0, 60.0
        };

        double[] sellThresholds = {
                40.0, 45.0, 50.0
        };

        int combinations = 0;

        for (int fastEma : fastEmaPeriods) {

            for (int slowEma : slowEmaPeriods) {

                /*
                 * StrategyConfig requires slow EMA to be
                 * greater than fast EMA.
                 */
                if (slowEma <= fastEma) {
                    continue;
                }

                for (int rsiPeriod : rsiPeriods) {

                    for (double buyThreshold : buyThresholds) {

                        for (double sellThreshold : sellThresholds) {

                            /*
                             * StrategyConfig requires:
                             *
                             * buy threshold > sell threshold
                             */
                            if (buyThreshold <= sellThreshold) {
                                continue;
                            }

                            StrategyConfig config =
                                    new StrategyConfig(
                                            fastEma,
                                            slowEma,
                                            rsiPeriod,
                                            buyThreshold,
                                            sellThreshold
                                    );

                            try {

                                Strategy strategy =
                                        createStrategy(
                                                series,
                                                config
                                        );

                                TradingRecord tradingRecord =
                                        backtestEngine.run(
                                                series,
                                                strategy
                                        );

                                double profit =
                                        new ProfitCriterion()
                                                .calculate(
                                                        series,
                                                        tradingRecord
                                                )
                                                .doubleValue();

                                combinations++;

                                log.debug(
                                        "Tested configuration: "
                                                + "EMA {}/{} | RSI {} | "
                                                + "Buy {} | Sell {} | "
                                                + "Profit {}",
                                        fastEma,
                                        slowEma,
                                        rsiPeriod,
                                        buyThreshold,
                                        sellThreshold,
                                        String.format(
                                                "%.2f",
                                                profit
                                        )
                                );

                                if (profit > bestProfit) {

                                    bestProfit = profit;
                                    bestConfig = config;

                                    log.info(
                                            "New best configuration: "
                                                    + "EMA {}/{} | RSI {} | "
                                                    + "Buy {} | Sell {} | "
                                                    + "Profit {}",
                                            fastEma,
                                            slowEma,
                                            rsiPeriod,
                                            buyThreshold,
                                            sellThreshold,
                                            String.format(
                                                    "%.2f",
                                                    profit
                                            )
                                    );
                                }

                            } catch (IllegalArgumentException e) {

                                log.warn(
                                        "Skipping invalid configuration: "
                                                + "EMA {}/{} | RSI {} | "
                                                + "Buy {} | Sell {}",
                                        fastEma,
                                        slowEma,
                                        rsiPeriod,
                                        buyThreshold,
                                        sellThreshold
                                );
                            }
                        }
                    }
                }
            }
        }

        if (bestConfig == null) {
            throw new IllegalStateException(
                    "Unable to find a valid strategy configuration"
            );
        }

        log.info(
                "Parameter optimization completed. "
                        + "Tested combinations: {}, "
                        + "best profit: {}",
                combinations,
                String.format("%.2f", bestProfit)
        );

        return bestConfig;
    }

    /**
     * Creates the strategy using the existing StrategyService.
     *
     * <p>This keeps strategy construction outside the optimizer.</p>
     */
    @Override
    public Strategy createStrategy(
            BarSeries series,
            StrategyConfig config) {

        Objects.requireNonNull(
                series,
                "BarSeries must not be null"
        );

        Objects.requireNonNull(
                config,
                "StrategyConfig must not be null"
        );

        return strategyService.getStrategy(
                StrategyType.EMA_RSI,
                series,
                config
        );
    }
}