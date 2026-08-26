package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import com.rithish.trading.config.StrategyConfig;
import com.rithish.trading.engine.BacktestEngine;
import com.rithish.trading.indicator.IndicatorRegistry;
import com.rithish.trading.loader.CsvMarketDataLoader;
import com.rithish.trading.model.StrategyType;
import com.rithish.trading.service.impl.StrategyService;
import com.rithish.trading.strategy.StrategyRegistry;

class StrategyEngineSmokeTest {

    @Test
    void tcsBacktestProducesSignals() {
        BarSeries series =
                new CsvMarketDataLoader().loadSeries("TCS");

        StrategyConfig config =
                new StrategyConfig(9, 21, 14, 55.0, 45.0);

        StrategyService service =
                new StrategyService(
                        new StrategyRegistry(
                                new IndicatorRegistry()
                        )
                );

        Strategy strategy = service.getStrategy(
                StrategyType.EMA_RSI,
                series,
                config
        );

        TradingRecord record =
                new BacktestEngine().run(series, strategy);

        assertTrue(
                record.getPositionCount() > 0
                        || record.getCurrentPosition().isOpened(),
                "EMA + RSI strategy should generate at least one position"
        );
    }
}
