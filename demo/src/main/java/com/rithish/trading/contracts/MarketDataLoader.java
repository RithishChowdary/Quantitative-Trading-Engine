package com.rithish.trading.contracts;

import org.ta4j.core.BarSeries;

public interface MarketDataLoader {

    BarSeries loadSeries(String symbol);
}