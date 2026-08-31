package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import com.rithish.trading.config.StrategyConfig;
import com.rithish.trading.indicator.IndicatorRegistry;
import com.rithish.trading.loader.CsvMarketDataLoader;
import com.rithish.trading.strategy.EmaRsiStrategyFactory;

/**
 * Unit tests for EmaRsiStrategyFactory.
 *
 * <p>Verifies strategy construction, input validation,
 * and minimum historical-data requirements.</p>
 */
class EmaRsiStrategyFactoryTest {

    private EmaRsiStrategyFactory factory;

    private BarSeries series;

    private StrategyConfig config;

    @BeforeEach
    void setUp() {

        IndicatorRegistry indicatorRegistry =
                new IndicatorRegistry();

        factory =
                new EmaRsiStrategyFactory(
                        indicatorRegistry
                );

        series =
                new CsvMarketDataLoader()
                        .loadSeries("TCS");

        config =
                new StrategyConfig(
                        9,
                        21,
                        14,
                        55.0,
                        45.0
                );
    }

    @Test
    void shouldRejectNullBarSeries() {

        assertThrows(
                NullPointerException.class,
                () -> factory.create(
                        null,
                        config
                )
        );
    }

    @Test
    void shouldRejectNullStrategyConfig() {

        assertThrows(
                NullPointerException.class,
                () -> factory.create(
                        series,
                        null
                )
        );
    }

    @Test
    void shouldRejectInsufficientBars() {

        BarSeries smallSeries =
                new CsvMarketDataLoader()
                        .loadSeries("TCS");

        /*
         * The factory requires:
         *
         * max(slow EMA, RSI period) + 1
         *
         * For the current configuration:
         *
         * max(21, 14) + 1 = 22 bars
         *
         * The real TCS series contains 721 bars, so this
         * test needs a deliberately small BarSeries.
         */
        BarSeries limitedSeries =
                smallSeries.getSubSeries(
                        0,
                        10
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> factory.create(
                        limitedSeries,
                        config
                )
        );
    }

    @Test
    void shouldCreateStrategySuccessfully() {

        Strategy strategy =
                factory.create(
                        series,
                        config
                );

        assertNotNull(strategy);
    }

    @Test
    void shouldCreateStrategyWithDifferentValidConfiguration() {

        StrategyConfig differentConfig =
                new StrategyConfig(
                        10,
                        26,
                        14,
                        60.0,
                        40.0
                );

        Strategy strategy =
                factory.create(
                        series,
                        differentConfig
                );

        assertNotNull(strategy);
    }
}