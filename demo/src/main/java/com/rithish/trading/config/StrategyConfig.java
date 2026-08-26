package com.rithish.trading.config;

import lombok.Getter;

/**
 * Immutable configuration for a trading strategy.
 *
 * <p>The RSI thresholds are used as momentum boundaries:</p>
 * <ul>
 *     <li>Buy when RSI is above the buy threshold.</li>
 *     <li>Exit when RSI falls below the sell threshold.</li>
 * </ul>
 */
@Getter
public final class StrategyConfig {

    private final int fastEmaPeriod;
    private final int slowEmaPeriod;
    private final int rsiPeriod;
    private final double rsiBuyThreshold;
    private final double rsiSellThreshold;

    public StrategyConfig(
            int fastEmaPeriod,
            int slowEmaPeriod,
            int rsiPeriod,
            double rsiBuyThreshold,
            double rsiSellThreshold) {

        validate(
                fastEmaPeriod,
                slowEmaPeriod,
                rsiPeriod,
                rsiBuyThreshold,
                rsiSellThreshold
        );

        this.fastEmaPeriod = fastEmaPeriod;
        this.slowEmaPeriod = slowEmaPeriod;
        this.rsiPeriod = rsiPeriod;
        this.rsiBuyThreshold = rsiBuyThreshold;
        this.rsiSellThreshold = rsiSellThreshold;
    }

    private void validate(
            int fastEmaPeriod,
            int slowEmaPeriod,
            int rsiPeriod,
            double rsiBuyThreshold,
            double rsiSellThreshold) {

        if (fastEmaPeriod <= 0) {
            throw new IllegalArgumentException(
                    "Fast EMA period must be greater than zero"
            );
        }

        if (slowEmaPeriod <= fastEmaPeriod) {
            throw new IllegalArgumentException(
                    "Slow EMA period must be greater than fast EMA period"
            );
        }

        if (rsiPeriod <= 0) {
            throw new IllegalArgumentException(
                    "RSI period must be greater than zero"
            );
        }

        if (!Double.isFinite(rsiBuyThreshold)
                || rsiBuyThreshold < 0
                || rsiBuyThreshold > 100) {
            throw new IllegalArgumentException(
                    "RSI buy threshold must be between 0 and 100"
            );
        }

        if (!Double.isFinite(rsiSellThreshold)
                || rsiSellThreshold < 0
                || rsiSellThreshold > 100) {
            throw new IllegalArgumentException(
                    "RSI sell threshold must be between 0 and 100"
            );
        }

        if (rsiBuyThreshold <= rsiSellThreshold) {
            throw new IllegalArgumentException(
                    "RSI buy threshold must be greater than RSI sell threshold"
            );
        }
    }
}
