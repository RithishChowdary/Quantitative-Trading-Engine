package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rithish.trading.contracts.StrategyFactory;
import com.rithish.trading.indicator.IndicatorRegistry;
import com.rithish.trading.model.StrategyType;
import com.rithish.trading.strategy.StrategyRegistry;

/**
 * Unit tests for StrategyRegistry.
 *
 * <p>Verifies that supported strategies are correctly
 * registered and invalid strategy types are rejected.</p>
 */
class StrategyRegistryTest {

    private StrategyRegistry registry;

    @BeforeEach
    void setUp() {

        IndicatorRegistry indicatorRegistry =
                new IndicatorRegistry();

        registry =
                new StrategyRegistry(
                        indicatorRegistry
                );
    }

    @Test
    void shouldReturnFactoryForEmaRsiStrategy() {

        StrategyFactory factory =
                registry.getFactory(
                        StrategyType.EMA_RSI
                );

        assertNotNull(factory);
    }

    @Test
    void shouldRejectNullStrategyType() {

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.getFactory(null)
        );
    }
}