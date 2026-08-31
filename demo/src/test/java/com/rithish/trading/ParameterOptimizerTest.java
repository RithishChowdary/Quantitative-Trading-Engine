package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import com.rithish.trading.config.StrategyConfig;
import com.rithish.trading.engine.BacktestEngine;
import com.rithish.trading.loader.CsvMarketDataLoader;
import com.rithish.trading.service.impl.StrategyService;
import com.rithish.trading.strategy.StrategyRegistry;
import com.rithish.trading.indicator.IndicatorRegistry;
import com.rithish.trading.optimizer.ParameterOptimizer;

class ParameterOptimizerTest {

    private ParameterOptimizer optimizer;
    private BarSeries series;

    @BeforeEach
    void setUp() {

        IndicatorRegistry indicatorRegistry =
                new IndicatorRegistry();

        StrategyRegistry strategyRegistry =
                new StrategyRegistry(indicatorRegistry);

        StrategyService strategyService =
                new StrategyService(strategyRegistry);

        BacktestEngine backtestEngine =
                new BacktestEngine();

        optimizer =
                new ParameterOptimizer(
                        strategyService,
                        backtestEngine
                );

        CsvMarketDataLoader loader =
                new CsvMarketDataLoader();

        series =
                loader.loadSeries("TCS");
    }

    @Test
    void shouldFindBestConfiguration() {

        StrategyConfig bestConfig =
                optimizer.optimize(series);

        assertNotNull(bestConfig);

        assertTrue(
                bestConfig.getFastEmaPeriod() > 0
        );

        assertTrue(
                bestConfig.getSlowEmaPeriod()
                        > bestConfig.getFastEmaPeriod()
        );

        assertTrue(
                bestConfig.getRsiPeriod() > 0
        );

        assertTrue(
                bestConfig.getRsiBuyThreshold()
                        > bestConfig.getRsiSellThreshold()
        );
    }

    @Test
    void shouldCreateStrategyFromConfiguration() {

        StrategyConfig config =
                new StrategyConfig(
                        9,
                        21,
                        14,
                        55.0,
                        45.0
                );

        Strategy strategy =
                optimizer.createStrategy(
                        series,
                        config
                );

        assertNotNull(strategy);

        assertEquals(
                "EMA_RSI",
                strategy.getName()
        );
    }

    @Test
    void shouldRejectNullSeriesForOptimization() {

        assertThrows(
                NullPointerException.class,
                () -> optimizer.optimize(null)
        );
    }

    @Test
    void shouldRejectNullSeriesWhenCreatingStrategy() {

        StrategyConfig config =
                new StrategyConfig(
                        9,
                        21,
                        14,
                        55.0,
                        45.0
                );

        assertThrows(
                NullPointerException.class,
                () -> optimizer.createStrategy(
                        null,
                        config
                )
        );
    }

    @Test
    void shouldRejectNullConfiguration() {

        assertThrows(
                NullPointerException.class,
                () -> optimizer.createStrategy(
                        series,
                        null
                )
        );
    }
}