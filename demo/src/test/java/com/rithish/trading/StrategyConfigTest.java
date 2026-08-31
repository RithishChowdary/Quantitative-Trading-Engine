package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.rithish.trading.config.StrategyConfig;

class StrategyConfigTest {

    @Test
    void shouldCreateValidConfiguration() {

        StrategyConfig config =
                new StrategyConfig(
                        9,
                        21,
                        14,
                        55.0,
                        45.0
                );

        assertEquals(9, config.getFastEmaPeriod());
        assertEquals(21, config.getSlowEmaPeriod());
        assertEquals(14, config.getRsiPeriod());
        assertEquals(55.0, config.getRsiBuyThreshold());
        assertEquals(45.0, config.getRsiSellThreshold());
    }

    @Test
    void shouldRejectZeroFastEmaPeriod() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyConfig(
                        0,
                        21,
                        14,
                        55.0,
                        45.0
                )
        );
    }

    @Test
    void shouldRejectNegativeFastEmaPeriod() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyConfig(
                        -1,
                        21,
                        14,
                        55.0,
                        45.0
                )
        );
    }

    @Test
    void shouldRejectSlowEmaNotGreaterThanFastEma() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyConfig(
                        21,
                        21,
                        14,
                        55.0,
                        45.0
                )
        );
    }

    @Test
    void shouldRejectSlowEmaSmallerThanFastEma() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyConfig(
                        21,
                        9,
                        14,
                        55.0,
                        45.0
                )
        );
    }

    @Test
    void shouldRejectZeroRsiPeriod() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyConfig(
                        9,
                        21,
                        0,
                        55.0,
                        45.0
                )
        );
    }

    @Test
    void shouldRejectNegativeRsiPeriod() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyConfig(
                        9,
                        21,
                        -1,
                        55.0,
                        45.0
                )
        );
    }

    @Test
    void shouldRejectBuyThresholdBelowZero() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyConfig(
                        9,
                        21,
                        14,
                        -1.0,
                        45.0
                )
        );
    }

    @Test
    void shouldRejectBuyThresholdAbove100() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyConfig(
                        9,
                        21,
                        14,
                        101.0,
                        45.0
                )
        );
    }

    @Test
    void shouldRejectSellThresholdBelowZero() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyConfig(
                        9,
                        21,
                        14,
                        55.0,
                        -1.0
                )
        );
    }

    @Test
    void shouldRejectSellThresholdAbove100() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyConfig(
                        9,
                        21,
                        14,
                        55.0,
                        101.0
                )
        );
    }

    @Test
    void shouldRejectBuyThresholdEqualToSellThreshold() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyConfig(
                        9,
                        21,
                        14,
                        50.0,
                        50.0
                )
        );
    }

    @Test
    void shouldRejectBuyThresholdBelowSellThreshold() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyConfig(
                        9,
                        21,
                        14,
                        40.0,
                        50.0
                )
        );
    }

    @Test
    void shouldAllowBoundaryThresholdValues() {

        assertDoesNotThrow(
                () -> new StrategyConfig(
                        9,
                        21,
                        14,
                        100.0,
                        0.0
                )
        );
    }
}