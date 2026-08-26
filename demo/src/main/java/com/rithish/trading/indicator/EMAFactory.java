package com.rithish.trading.indicator;

import com.rithish.trading.contracts.IndicatorFactory;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class EMAFactory implements IndicatorFactory {

    @Override
    public Indicator<Num> create(
            ClosePriceIndicator closePrice,
            int period) {

        return new EMAIndicator(
                closePrice,
                period
        );
    }
}