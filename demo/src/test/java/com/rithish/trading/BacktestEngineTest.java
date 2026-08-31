package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.BaseBarSeriesBuilder;

import com.rithish.trading.config.StrategyConfig;
import com.rithish.trading.engine.BacktestEngine;
import com.rithish.trading.exceptions.BacktestExecutionException;
import com.rithish.trading.indicator.IndicatorRegistry;
import com.rithish.trading.loader.CsvMarketDataLoader;
import com.rithish.trading.model.StrategyType;
import com.rithish.trading.service.impl.StrategyService;
import com.rithish.trading.strategy.StrategyRegistry;

/**
 * Unit tests for BacktestEngine.
 *
 * <p>Tests validation, successful execution and
 * exception handling during backtest execution.</p>
 */
class BacktestEngineTest {

    private BacktestEngine backtestEngine;

    @BeforeEach
    void setUp() {
        backtestEngine = new BacktestEngine();
    }

    @Test
    void shouldRejectNullBarSeries() {

        Strategy strategy =
                createSimpleStrategy();

        assertThrows(
                IllegalArgumentException.class,
                () -> backtestEngine.run(
                        null,
                        strategy
                )
        );
    }

    @Test
    void shouldRejectEmptyBarSeries() {

        BarSeries series =
                new BaseBarSeriesBuilder()
                        .withName("EMPTY")
                        .build();

        Strategy strategy =
                createSimpleStrategy();

        assertThrows(
                IllegalArgumentException.class,
                () -> backtestEngine.run(
                        series,
                        strategy
                )
        );
    }

   @Test
   void shouldRejectNullStrategy() {

    BarSeries series =
            new CsvMarketDataLoader()
                    .loadSeries("TCS");

    assertThrows(
            IllegalArgumentException.class,
            () -> backtestEngine.run(
                    series,
                    null
            )
    );
}
    @Test
    void shouldExecuteValidBacktest() {

        CsvMarketDataLoader loader =
                new CsvMarketDataLoader();

        BarSeries series =
                loader.loadSeries("TCS");

        IndicatorRegistry indicatorRegistry =
                new IndicatorRegistry();

        StrategyRegistry strategyRegistry =
                new StrategyRegistry(
                        indicatorRegistry
                );

        StrategyService strategyService =
                new StrategyService(
                        strategyRegistry
                );

        StrategyConfig config =
                new StrategyConfig(
                        9,
                        21,
                        14,
                        55.0,
                        45.0
                );

        Strategy strategy =
                strategyService.getStrategy(
                        StrategyType.EMA_RSI,
                        series,
                        config
                );

        TradingRecord tradingRecord =
                assertDoesNotThrow(
                        () -> backtestEngine.run(
                                series,
                                strategy
                        )
                );

        assertNotNull(tradingRecord);

        assertTrue(
                tradingRecord.getPositionCount() >= 0
        );
    }

    @Test
    void shouldWrapRuntimeExceptionAsBacktestExecutionException() {

        BarSeries series =
                new CsvMarketDataLoader()
                        .loadSeries("TCS");

        Strategy failingStrategy =
                new BaseStrategy(
                        "FAILING_STRATEGY",
                        (index, tradingRecord) -> {
                            throw new RuntimeException(
                                    "Simulated strategy failure"
                            );
                        },
                        (index, tradingRecord) -> false
                );

        BacktestExecutionException exception =
                assertThrows(
                        BacktestExecutionException.class,
                        () -> backtestEngine.run(
                                series,
                                failingStrategy
                        )
                );

        assertNotNull(exception.getCause());

        assertTrue(
                exception.getCause()
                        .getMessage()
                        .contains(
                                "Simulated strategy failure"
                        )
        );
    }

    /**
     * Creates a strategy that never enters or exits.
     *
     * <p>Used for tests where the strategy itself is not
     * the subject under test.</p>
     */
    private Strategy createSimpleStrategy() {

        return new BaseStrategy(
                "TEST_STRATEGY",
                (index, tradingRecord) -> false,
                (index, tradingRecord) -> false
        );
    }
}