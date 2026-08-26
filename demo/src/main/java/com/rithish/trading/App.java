package com.rithish.trading;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import com.rithish.trading.config.StrategyConfig;
import com.rithish.trading.contracts.MarketDataLoader;
import com.rithish.trading.engine.BacktestEngine;
import com.rithish.trading.indicator.IndicatorRegistry;
import com.rithish.trading.loader.CsvMarketDataLoader;
import com.rithish.trading.model.StrategyType;
import com.rithish.trading.report.PerformanceMetrics;
import com.rithish.trading.report.PerformanceMetricsCalculator;
import com.rithish.trading.report.PerformanceReportService;
import com.rithish.trading.report.TradeReportService;
import com.rithish.trading.service.impl.StrategyService;
import com.rithish.trading.strategy.StrategyRegistry;

/**
 * Console entry point for the quantitative trading engine.
 */
public class App {

    private static final String DEFAULT_SYMBOL = "TCS";

    public static void main(String[] args) {

        String symbol = args.length > 0
                ? args[0].trim().toUpperCase()
                : DEFAULT_SYMBOL;

        // EMA 9/21 + RSI 14 momentum strategy
        StrategyConfig config =
                new StrategyConfig(
                        9,
                        21,
                        14,
                        55.0,
                        45.0
                );

        /*
         * 1. Load market data
         */
        MarketDataLoader loader =
                new CsvMarketDataLoader();

        BarSeries series =
                loader.loadSeries(symbol);

        /*
         * 2. Create indicator registry
         */
        IndicatorRegistry indicatorRegistry =
                new IndicatorRegistry();

        /*
         * 3. Create strategy registry
         */
        StrategyRegistry strategyRegistry =
                new StrategyRegistry(indicatorRegistry);

        /*
         * 4. Create strategy service
         */
        StrategyService strategyService =
                new StrategyService(strategyRegistry);

        /*
         * 5. Build strategy
         */
        Strategy strategy =
                strategyService.getStrategy(
                        StrategyType.EMA_RSI,
                        series,
                        config
                );

        /*
         * 6. Execute backtest
         */
        BacktestEngine backtestEngine =
                new BacktestEngine();

        TradingRecord tradingRecord =
                backtestEngine.run(
                        series,
                        strategy
                );

        /*
         * 7. Calculate performance metrics
         */
        PerformanceMetricsCalculator metricsCalculator =
                new PerformanceMetricsCalculator();

        PerformanceMetrics metrics =
                metricsCalculator.calculate(
                        series,
                        tradingRecord
                );

        /*
         * 8. Print performance report
         */
        new PerformanceReportService()
                .print(
                        series,
                        tradingRecord,
                        metrics
                );

        /*
         * 9. Print individual trade details
         */
        new TradeReportService()
                .print(
                        series,
                        tradingRecord
                );
    }
}