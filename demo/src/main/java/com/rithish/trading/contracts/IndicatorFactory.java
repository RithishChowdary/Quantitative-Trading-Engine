package com.rithish.trading.contracts;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

public interface IndicatorFactory {

    Indicator<Num> create(
            ClosePriceIndicator closePrice,
            int period
    );
}