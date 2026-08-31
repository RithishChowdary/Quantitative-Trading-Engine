package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.rithish.trading.contracts.IndicatorFactory;
import com.rithish.trading.indicator.EMAFactory;
import com.rithish.trading.indicator.IndicatorRegistry;
import com.rithish.trading.indicator.RSIFactory;
import com.rithish.trading.model.IndicatorType;

class IndicatorRegistryTest {

    private final IndicatorRegistry registry =
            new IndicatorRegistry();

    @Test
    void shouldReturnEmaFactory() {

        IndicatorFactory factory =
                registry.getFactory(IndicatorType.EMA);

        assertNotNull(factory);
        assertInstanceOf(EMAFactory.class, factory);
    }

    @Test
    void shouldReturnRsiFactory() {

        IndicatorFactory factory =
                registry.getFactory(IndicatorType.RSI);

        assertNotNull(factory);
        assertInstanceOf(RSIFactory.class, factory);
    }

    @Test
    void shouldRejectNullIndicatorType() {

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.getFactory(null)
        );
    }
}