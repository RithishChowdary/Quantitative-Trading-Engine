package com.rithish.trading.report;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.criteria.MaximumDrawdownCriterion;

/**
 * Calculates quantitative performance metrics from a backtest result.
 *
 * Calculation logic is kept outside PerformanceMetrics so that the
 * metrics object remains a simple data holder.
 */
@Component
public class PerformanceMetricsCalculator {

    public PerformanceMetrics calculate(
            BarSeries series,
            TradingRecord tradingRecord) {

        Objects.requireNonNull(
                series,
                "BarSeries must not be null"
        );

        Objects.requireNonNull(
                tradingRecord,
                "TradingRecord must not be null"
        );

        int totalTrades =
                tradingRecord.getPositionCount();

        double totalProfit = 0.0;
        double totalLoss = 0.0;

        int winningTrades = 0;
        int losingTrades = 0;

        /*
         * Calculate gross profit and gross loss
         * from completed positions.
         */
        for (Position position : tradingRecord.getPositions()) {

            if (position == null
                    || position.getEntry() == null
                    || position.getExit() == null) {
                continue;
            }

            double profitLoss =
                    position.getProfit().doubleValue();

            if (profitLoss > 0) {

                totalProfit += profitLoss;
                winningTrades++;

            } else if (profitLoss < 0) {

                // Store loss as a positive reporting value.
                totalLoss += Math.abs(profitLoss);
                losingTrades++;
            }
        }

        /*
         * Net earnings after losses.
         *
         * Example:
         * Profit = 543.40
         * Loss   = 408.65
         * Earnings = 134.75
         */
        double totalEarnings =
                totalProfit - totalLoss;

        /*
         * Win rate.
         */
        double winRate =
                totalTrades == 0
                        ? 0.0
                        : (double) winningTrades / totalTrades;

        /*
         * Average net earnings per completed trade.
         */
        double averageProfit =
                totalTrades == 0
                        ? 0.0
                        : totalEarnings / totalTrades;

        /*
         * Maximum drawdown.
         */
        double maximumDrawdown =
                totalTrades == 0
                        ? 0.0
                        : new MaximumDrawdownCriterion()
                                .calculate(
                                        series,
                                        tradingRecord
                                )
                                .doubleValue();

        /*
         * Profit factor.
         */
        double profitFactor =
                totalLoss == 0.0
                        ? (totalProfit > 0.0
                                ? Double.POSITIVE_INFINITY
                                : 0.0)
                        : totalProfit / totalLoss;

        return new PerformanceMetrics(
                totalTrades,
                winningTrades,
                losingTrades,
                winRate,
                totalProfit,
                totalLoss,
                totalEarnings,
                averageProfit,
                maximumDrawdown,
                profitFactor
        );
    }
}