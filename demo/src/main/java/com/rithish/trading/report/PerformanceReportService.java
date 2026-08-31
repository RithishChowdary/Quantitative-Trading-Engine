package com.rithish.trading.report;

import java.util.Objects;

import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;

/**
 * Prints a human-readable quantitative backtest summary.
 */
public class PerformanceReportService {

    public void print(
            BarSeries series,
            TradingRecord tradingRecord,
            PerformanceMetrics metrics) {

        Objects.requireNonNull(
                series,
                "BarSeries must not be null"
        );

        Objects.requireNonNull(
                tradingRecord,
                "TradingRecord must not be null"
        );

        Objects.requireNonNull(
                metrics,
                "PerformanceMetrics must not be null"
        );

        System.out.println();
        System.out.println("===== BACKTEST REPORT =====");

        System.out.println(
                "Series Name       : "
                        + series.getName()
        );

        System.out.println(
                "Bars              : "
                        + series.getBarCount()
        );

        System.out.println(
                "Completed Trades  : "
                        + metrics.getTotalTrades()
        );

        System.out.println(
                "Winning Trades    : "
                        + metrics.getWinningTrades()
        );

        System.out.println(
                "Losing Trades     : "
                        + metrics.getLosingTrades()
        );

        System.out.printf(
                "Win Rate          : %.2f%%%n",
                metrics.getWinRate() * 100
        );

              System.out.printf(
                "Total Profit      : %.2f%n",
                metrics.getTotalProfit()
        );

        System.out.printf(
                "Total Loss        : %.2f%n",
                metrics.getTotalLoss()
        );

        System.out.printf(
                "Total Earnings    : %.2f%n",
                metrics.getTotalEarnings()
        );

        System.out.printf(
                "Average Profit    : %.2f%n",
                metrics.getAverageProfit()
        );

        System.out.printf(
                "Maximum Drawdown  : %.2f%n",
                metrics.getMaximumDrawdown()
        );

        System.out.printf(
                "Profit Factor     : %.2f%n",
                metrics.getProfitFactor()
        );

        System.out.println(
                "Current Position  : "
                        + (tradingRecord
                                .getCurrentPosition()
                                .isOpened()
                                ? "OPEN"
                                : "CLOSED")
        );

        System.out.println("===========================");
    }
}