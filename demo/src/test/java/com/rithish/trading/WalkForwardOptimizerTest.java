package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import com.rithish.trading.config.StrategyConfig;
import com.rithish.trading.engine.BacktestEngine;
import com.rithish.trading.indicator.IndicatorRegistry;
import com.rithish.trading.loader.CsvMarketDataLoader;
import com.rithish.trading.optimizer.ParameterOptimizer;
import com.rithish.trading.optimizer.WalkForwardOptimizer;
import com.rithish.trading.optimizer.WalkForwardOptimizer.WalkForwardResult;
import com.rithish.trading.service.impl.StrategyService;
import com.rithish.trading.strategy.StrategyRegistry;

class WalkForwardOptimizerTest {

    private BarSeries loadTcsSeries() {

        CsvMarketDataLoader loader =
                new CsvMarketDataLoader();

        return loader.loadSeries("TCS");
    }

    private WalkForwardOptimizer createOptimizer() {

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

        BacktestEngine backtestEngine =
                new BacktestEngine();

        ParameterOptimizer parameterOptimizer =
                new ParameterOptimizer(
                        strategyService,
                        backtestEngine
                );

        return new WalkForwardOptimizer(
                parameterOptimizer,
                backtestEngine,
                400,
                100,
                100
        );
    }

    @Test
    void shouldPerformWalkForwardOptimization() {

        BarSeries series =
                loadTcsSeries();

        WalkForwardOptimizer optimizer =
                createOptimizer();

        List<WalkForwardResult> results =
                optimizer.optimize(series);

        assertFalse(results.isEmpty());

        assertEquals(
                3,
                results.size()
        );
    }

    @Test
    void shouldCreateCorrectFirstWalkForwardWindow() {

        BarSeries series =
                loadTcsSeries();

        WalkForwardOptimizer optimizer =
                createOptimizer();

        List<WalkForwardResult> results =
                optimizer.optimize(series);

        WalkForwardResult first =
                results.get(0);

        assertEquals(
                1,
                first.getWindowNumber()
        );

        assertEquals(
                0,
                first.getTrainingStart()
        );

        assertEquals(
                400,
                first.getTrainingEnd()
        );

        assertEquals(
                400,
                first.getTestingStart()
        );

        assertEquals(
                500,
                first.getTestingEnd()
        );
    }

    @Test
    void shouldMoveWindowForwardCorrectly() {

        BarSeries series =
                loadTcsSeries();

        WalkForwardOptimizer optimizer =
                createOptimizer();

        List<WalkForwardResult> results =
                optimizer.optimize(series);

        WalkForwardResult first =
                results.get(0);

        WalkForwardResult second =
                results.get(1);

        assertEquals(
                1,
                first.getWindowNumber()
        );

        assertEquals(
                2,
                second.getWindowNumber()
        );

        assertEquals(
                0,
                first.getTrainingStart()
        );

        assertEquals(
                100,
                second.getTrainingStart()
        );

        assertEquals(
                400,
                first.getTestingStart()
        );

        assertEquals(
                500,
                second.getTestingStart()
        );
    }

    @Test
    void shouldReturnValidBestConfiguration() {

        BarSeries series =
                loadTcsSeries();

        WalkForwardOptimizer optimizer =
                createOptimizer();

        List<WalkForwardResult> results =
                optimizer.optimize(series);

        for (WalkForwardResult result : results) {

            StrategyConfig config =
                    result.getBestConfig();

            assertNotNull(config);

            assertTrue(
                    config.getFastEmaPeriod() > 0
            );

            assertTrue(
                    config.getSlowEmaPeriod()
                            > config.getFastEmaPeriod()
            );

            assertTrue(
                    config.getRsiPeriod() > 0
            );

            assertTrue(
                    config.getRsiBuyThreshold()
                            > config.getRsiSellThreshold()
            );
        }
    }

    @Test
    void shouldCalculateTotalTestingProfit() {

        BarSeries series =
                loadTcsSeries();

        WalkForwardOptimizer optimizer =
                createOptimizer();

        List<WalkForwardResult> results =
                optimizer.optimize(series);

        double totalProfit =
                optimizer.calculateTotalProfit(
                        results
                );

        double expectedProfit =
                results.stream()
                        .mapToDouble(
                                WalkForwardResult::getTestingProfit
                        )
                        .sum();

        assertEquals(
                expectedProfit,
                totalProfit,
                0.000001
        );
    }

    @Test
    void shouldReturnUnmodifiableResults() {

        BarSeries series =
                loadTcsSeries();

        WalkForwardOptimizer optimizer =
                createOptimizer();

        List<WalkForwardResult> results =
                optimizer.optimize(series);

        assertThrows(
                UnsupportedOperationException.class,
                () -> results.clear()
        );
    }

    @Test
    void shouldRejectNullSeries() {

        WalkForwardOptimizer optimizer =
                createOptimizer();

        assertThrows(
                NullPointerException.class,
                () -> optimizer.optimize(null)
        );
    }

    @Test
    void shouldRejectEmptySeries() {

        BarSeries emptySeries =
                new org.ta4j.core.BaseBarSeriesBuilder()
                        .withName("EMPTY")
                        .build();

        WalkForwardOptimizer optimizer =
                createOptimizer();

        assertThrows(
                IllegalArgumentException.class,
                () -> optimizer.optimize(emptySeries)
        );
    }

    @Test
    void shouldRejectInsufficientBars() {

        BarSeries series =
                loadTcsSeries();

        WalkForwardOptimizer optimizer =
                new WalkForwardOptimizer(
                        new ParameterOptimizer(
                                new StrategyService(
                                        new StrategyRegistry(
                                                new IndicatorRegistry()
                                        )
                                ),
                                new BacktestEngine()
                        ),
                        new BacktestEngine(),
                        700,
                        100,
                        100
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> optimizer.optimize(series)
        );
    }

    @Test
    void shouldRejectInvalidTrainingWindow() {

        BacktestEngine backtestEngine =
                new BacktestEngine();

        ParameterOptimizer parameterOptimizer =
                new ParameterOptimizer(
                        new StrategyService(
                                new StrategyRegistry(
                                        new IndicatorRegistry()
                                )
                        ),
                        backtestEngine
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new WalkForwardOptimizer(
                                parameterOptimizer,
                                backtestEngine,
                                0,
                                100,
                                100
                        )
        );
    }

    @Test
    void shouldRejectInvalidTestingWindow() {

        BacktestEngine backtestEngine =
                new BacktestEngine();

        ParameterOptimizer parameterOptimizer =
                new ParameterOptimizer(
                        new StrategyService(
                                new StrategyRegistry(
                                        new IndicatorRegistry()
                                )
                        ),
                        backtestEngine
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new WalkForwardOptimizer(
                                parameterOptimizer,
                                backtestEngine,
                                400,
                                0,
                                100
                        )
        );
    }

    @Test
    void shouldRejectInvalidStepSize() {

        BacktestEngine backtestEngine =
                new BacktestEngine();

        ParameterOptimizer parameterOptimizer =
                new ParameterOptimizer(
                        new StrategyService(
                                new StrategyRegistry(
                                        new IndicatorRegistry()
                                )
                        ),
                        backtestEngine
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new WalkForwardOptimizer(
                                parameterOptimizer,
                                backtestEngine,
                                400,
                                100,
                                0
                        )
        );
    }
}