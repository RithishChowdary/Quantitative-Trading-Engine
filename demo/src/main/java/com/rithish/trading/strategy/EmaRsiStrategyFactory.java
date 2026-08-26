package com.rithish.trading.strategy;

import java.util.Objects;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import com.rithish.trading.config.StrategyConfig;
import com.rithish.trading.contracts.StrategyFactory;
import com.rithish.trading.indicator.IndicatorRegistry;
import com.rithish.trading.model.IndicatorType;

import lombok.RequiredArgsConstructor;

/**
 * Builds the EMA + RSI momentum strategy.
 *
 * <p>Entry:</p>
 * <ul>
 *     <li>Fast EMA crosses above slow EMA.</li>
 *     <li>RSI is above the buy threshold.</li>
 * </ul>
 *
 * <p>Exit:</p>
 * <ul>
 *     <li>Fast EMA crosses below slow EMA, OR</li>
 *     <li>RSI falls below the sell threshold.</li>
 * </ul>
 */
@RequiredArgsConstructor
public class EmaRsiStrategyFactory implements StrategyFactory {

    private final IndicatorRegistry indicatorRegistry;

    @Override
    public Strategy create(
            BarSeries series,
            StrategyConfig config) {

        Objects.requireNonNull(series, "BarSeries must not be null");
        Objects.requireNonNull(config, "StrategyConfig must not be null");

        int minimumBars = Math.max(
                config.getSlowEmaPeriod(),
                config.getRsiPeriod()
        ) + 1;

        if (series.getBarCount() < minimumBars) {
            throw new IllegalArgumentException(
                    "Not enough bars to build EMA + RSI strategy. "
                            + "Required at least " + minimumBars
                            + ", found " + series.getBarCount()
            );
        }

        ClosePriceIndicator closePrice =
                new ClosePriceIndicator(series);

        EMAIndicator fastEma =
                (EMAIndicator) indicatorRegistry
                        .getFactory(IndicatorType.EMA)
                        .create(
                                closePrice,
                                config.getFastEmaPeriod()
                        );

        EMAIndicator slowEma =
                (EMAIndicator) indicatorRegistry
                        .getFactory(IndicatorType.EMA)
                        .create(
                                closePrice,
                                config.getSlowEmaPeriod()
                        );

        RSIIndicator rsi =
                (RSIIndicator) indicatorRegistry
                        .getFactory(IndicatorType.RSI)
                        .create(
                                closePrice,
                                config.getRsiPeriod()
                        );

        var entryRule =
                new CrossedUpIndicatorRule(
                        fastEma,
                        slowEma
                ).and(
                        new OverIndicatorRule(
                                rsi,
                                config.getRsiBuyThreshold()
                        )
                );

        var exitRule =
                new CrossedDownIndicatorRule(
                        fastEma,
                        slowEma
                ).or(
                        new UnderIndicatorRule(
                                rsi,
                                config.getRsiSellThreshold()
                        )
                );

        return new BaseStrategy(
                "EMA_RSI",
                entryRule,
                exitRule
        );
    }
}
