package com.rithish.trading.engine;

import com.rithish.trading.exceptions.BacktestExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.BarSeriesManager;

/** Executes a strategy against a historical bar series. */
@Component
@Slf4j
public class BacktestEngine {

    public TradingRecord run(
            BarSeries series,
            Strategy strategy) {

        validate(series, strategy);

        log.info(
                "Starting backtest. Series: {}, bars: {}",
                series.getName(),
                series.getBarCount()
        );

        try {
            BarSeriesManager manager =
                    new BarSeriesManager(series);

            TradingRecord tradingRecord =
                    manager.run(strategy);

            log.info(
                    "Backtest completed. Completed positions: {}, open position: {}",
                    tradingRecord.getPositionCount(),
                    tradingRecord.getCurrentPosition().isOpened()
            );

            return tradingRecord;

        } catch (RuntimeException e) {
            log.error(
                    "Backtest execution failed for series: {}",
                    series.getName(),
                    e
            );

            throw new BacktestExecutionException(
                    "Failed to execute backtest",
                    e
            );
        }
    }

    private void validate(
            BarSeries series,
            Strategy strategy) {

        if (series == null) {
            throw new IllegalArgumentException(
                    "BarSeries must not be null"
            );
        }

        if (series.isEmpty()) {
            throw new IllegalArgumentException(
                    "BarSeries must contain at least one bar"
            );
        }

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Strategy must not be null"
            );
        }
    }
}
