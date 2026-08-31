package com.rithish.trading.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import com.rithish.trading.config.StrategyConfig;
import com.rithish.trading.dto.api.BacktestRequest;
import com.rithish.trading.dto.api.BacktestResponse;
import com.rithish.trading.dto.api.WalkForwardBacktestResponse;
import com.rithish.trading.dto.api.WalkForwardWindowResponse;
import com.rithish.trading.engine.BacktestEngine;
import com.rithish.trading.indicator.IndicatorRegistry;
import com.rithish.trading.loader.ApiMarketDataLoader;
import com.rithish.trading.optimizer.ParameterOptimizer;
import com.rithish.trading.optimizer.WalkForwardOptimizer;
import com.rithish.trading.report.PerformanceMetrics;
import com.rithish.trading.report.PerformanceMetricsCalculator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BacktestApiService {

    private final ApiMarketDataLoader marketDataLoader;

    private final BacktestEngine backtestEngine;

    private final PerformanceMetricsCalculator metricsCalculator;

    private final ParameterOptimizer parameterOptimizer;

    private final WalkForwardOptimizer walkForwardOptimizer;

    public BacktestApiService(
            ApiMarketDataLoader marketDataLoader,
            BacktestEngine backtestEngine,
            PerformanceMetricsCalculator metricsCalculator,
            IndicatorRegistry indicatorRegistry,
            ParameterOptimizer parameterOptimizer) {

        this.marketDataLoader =
                Objects.requireNonNull(
                        marketDataLoader,
                        "ApiMarketDataLoader must not be null"
                );

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

        Objects.requireNonNull(
                indicatorRegistry,
                "IndicatorRegistry must not be null"
        );

        this.parameterOptimizer =
                Objects.requireNonNull(
                        parameterOptimizer,
                        "ParameterOptimizer must not be null"
                );

        /*
         * Walk-forward configuration:
         *
         * Training window = 50 bars
         * Testing window  = 25 bars
         * Step size       = 25 bars
         *
         * Alpha Vantage compact data provides approximately
         * 100 daily bars.
         */
        this.walkForwardOptimizer =
                new WalkForwardOptimizer(
                        this.parameterOptimizer,
                        this.backtestEngine,
                        50,
                        25,
                        25
                );
    }

    /**
     * Executes a normal API backtest.
     *
     * <p>
     * Historical market data is loaded from the API.
     * The ParameterOptimizer searches multiple EMA + RSI
     * configurations and selects the configuration with
     * the highest historical profit.
     * </p>
     *
     * @param request backtest request containing the symbol
     * @return backtest response containing performance metrics
     */
    public BacktestResponse execute(
            BacktestRequest request) {

        Objects.requireNonNull(
                request,
                "BacktestRequest must not be null"
        );

        if (request.getSymbol() == null
                || request.getSymbol().isBlank()) {

            throw new IllegalArgumentException(
                    "Symbol must not be empty"
            );
        }

        String symbol =
                request.getSymbol()
                        .trim()
                        .toUpperCase();

        log.info(
                "Starting API backtest for symbol: {}",
                symbol
        );

        /*
         * 1. Load historical market data.
         */
        BarSeries series =
                marketDataLoader.loadSeries(
                        symbol
                );

        /*
         * 2. Optimize strategy parameters.
         */
        StrategyConfig bestConfig =
                parameterOptimizer.optimize(
                        series
                );

        log.info(
                "Best strategy configuration for {}: "
                        + "EMA {}/{} | RSI {} | "
                        + "Buy {} | Sell {}",
                symbol,
                bestConfig.getFastEmaPeriod(),
                bestConfig.getSlowEmaPeriod(),
                bestConfig.getRsiPeriod(),
                bestConfig.getRsiBuyThreshold(),
                bestConfig.getRsiSellThreshold()
        );

        /*
         * 3. Create strategy using optimized configuration.
         */
        Strategy strategy =
                parameterOptimizer.createStrategy(
                        series,
                        bestConfig
                );

        /*
         * 4. Execute backtest.
         */
        TradingRecord tradingRecord =
                backtestEngine.run(
                        series,
                        strategy
                );

        /*
         * 5. Calculate performance metrics.
         */
        PerformanceMetrics metrics =
                metricsCalculator.calculate(
                        series,
                        tradingRecord
                );

        /*
         * 6. Convert internal result into API DTO.
         */
        BacktestResponse response =
                new BacktestResponse(
                        symbol,
                        metrics
                );

        log.info(
                "API backtest completed for {}. "
                        + "Trades: {}, Earnings: {}",
                symbol,
                metrics.getTotalTrades(),
                metrics.getTotalEarnings()
        );

        return response;
    }

    /**
     * Executes walk-forward optimization using market data
     * obtained from the API.
     *
     * <p>
     * For every walk-forward window:
     *
     * <ol>
     *     <li>Optimize parameters using training data.</li>
     *     <li>Create the strategy using the selected parameters.</li>
     *     <li>Evaluate the strategy on unseen testing data.</li>
     * </ol>
     *
     * @param request backtest request containing the symbol
     * @return walk-forward backtest response
     */
    public WalkForwardBacktestResponse executeWalkForward(
            BacktestRequest request) {

        Objects.requireNonNull(
                request,
                "BacktestRequest must not be null"
        );

        if (request.getSymbol() == null
                || request.getSymbol().isBlank()) {

            throw new IllegalArgumentException(
                    "Symbol must not be empty"
            );
        }

        String symbol =
                request.getSymbol()
                        .trim()
                        .toUpperCase();

        log.info(
                "Starting walk-forward API backtest for symbol: {}",
                symbol
        );

        /*
         * 1. Load historical market data.
         */
        BarSeries series =
                marketDataLoader.loadSeries(
                        symbol
                );

        /*
         * 2. Execute walk-forward optimization.
         *
         * Training window = 50
         * Testing window  = 25
         * Step size       = 25
         */
        List<WalkForwardOptimizer.WalkForwardResult>
                optimizerResults =
                walkForwardOptimizer.optimize(
                        series
                );

        /*
         * 3. Convert internal optimizer results
         *    into API DTOs.
         */
        List<WalkForwardWindowResponse> results =
                optimizerResults.stream()
                        .map(result ->
                                new WalkForwardWindowResponse(
                                        result.getWindowNumber(),

                                        result.getTrainingStart(),
                                        result.getTrainingEnd(),

                                        result.getTestingStart(),
                                        result.getTestingEnd(),

                                        result.getBestConfig()
                                                .getFastEmaPeriod(),

                                        result.getBestConfig()
                                                .getSlowEmaPeriod(),

                                        result.getBestConfig()
                                                .getRsiPeriod(),

                                        result.getBestConfig()
                                                .getRsiBuyThreshold(),

                                        result.getBestConfig()
                                                .getRsiSellThreshold(),

                                        result.getTestingProfit(),

                                        result.getCompletedTrades()
                                )
                        )
                        .toList();

        /*
         * 4. Calculate total out-of-sample profit.
         */
        double totalOutOfSampleProfit =
                walkForwardOptimizer.calculateTotalProfit(
                        optimizerResults
                );

        /*
         * 5. Build API response.
         */
        WalkForwardBacktestResponse response =
                new WalkForwardBacktestResponse(
                        symbol,
                        results.size(),
                        totalOutOfSampleProfit,
                        results
                );

        log.info(
                "Walk-forward API backtest completed for {}. "
                        + "Windows: {}, Total OOS Profit: {}",
                symbol,
                results.size(),
                String.format(
                        "%.2f",
                        totalOutOfSampleProfit
                )
        );

        return response;
    }
}