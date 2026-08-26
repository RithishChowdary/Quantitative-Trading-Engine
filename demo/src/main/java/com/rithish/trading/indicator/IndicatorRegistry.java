package com.rithish.trading.indicator;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.rithish.trading.contracts.IndicatorFactory;
import com.rithish.trading.model.IndicatorType;

@Component
public class IndicatorRegistry {

    private final Map<IndicatorType, IndicatorFactory> registry;

    public IndicatorRegistry() {

        this.registry =
                new EnumMap<>(IndicatorType.class);

        registry.put(
                IndicatorType.EMA,
                new EMAFactory()
        );

        registry.put(
                IndicatorType.RSI,
                new RSIFactory()
        );
    }

    public IndicatorFactory getFactory(
            IndicatorType type) {

        if (type == null) {
            throw new IllegalArgumentException(
                    "Indicator type must not be null"
            );
        }

        IndicatorFactory factory =
                registry.get(type);

        if (factory == null) {
            throw new IllegalArgumentException(
                    "No indicator registered for type: "
                            + type
            );
        }

        return factory;
    }
}