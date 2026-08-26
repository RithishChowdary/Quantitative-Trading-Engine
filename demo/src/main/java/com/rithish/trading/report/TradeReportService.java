package com.rithish.trading.report;

import java.util.Objects;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;

/**
 * Prints detailed information about completed and currently open trades.
 */
public class TradeReportService {

    public void print(
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

        System.out.println();
        System.out.println("===== TRADE DETAILS =====");

        int tradeNumber = 1;

        /*
         * Completed positions
         */
        for (Position position : tradingRecord.getPositions()) {

            if (position == null) {
                continue;
            }

            Trade entry = position.getEntry();
            Trade exit = position.getExit();

            if (entry == null || exit == null) {
                continue;
            }

            int entryIndex = entry.getIndex();
            int exitIndex = exit.getIndex();

            double entryPrice =
                    entry.getNetPrice().doubleValue();

            double exitPrice =
                    exit.getNetPrice().doubleValue();

            double profitLoss =
                    position.getProfit().doubleValue();

            int holdingBars =
                    exitIndex - entryIndex;

            System.out.println();
            System.out.println("Trade #" + tradeNumber);

            System.out.println(
                    "Entry Date    : "
                            + series.getBar(entryIndex)
                            .getEndTime()
            );

            System.out.printf(
                    "Entry Price   : %.2f%n",
                    entryPrice
            );

            System.out.println(
                    "Exit Date     : "
                            + series.getBar(exitIndex)
                            .getEndTime()
            );

            System.out.printf(
                    "Exit Price    : %.2f%n",
                    exitPrice
            );

            System.out.printf(
                    "Profit/Loss   : %.2f%n",
                    profitLoss
            );

            System.out.println(
                    "Holding Bars  : "
                            + holdingBars
            );

            tradeNumber++;
        }

        /*
         * Currently open position
         */
        Position currentPosition =
                tradingRecord.getCurrentPosition();

        if (currentPosition != null
                && currentPosition.isOpened()) {

            Trade entry =
                    currentPosition.getEntry();

            if (entry != null) {

                int entryIndex =
                        entry.getIndex();

                double entryPrice =
                        entry.getNetPrice().doubleValue();

                int currentIndex =
                        series.getEndIndex();

                double currentPrice =
                        series.getBar(currentIndex)
                                .getClosePrice()
                                .doubleValue();

                double unrealizedProfit =
                        currentPrice - entryPrice;

                int holdingBars =
                        currentIndex - entryIndex;

                System.out.println();
                System.out.println("===== OPEN POSITION =====");

                System.out.println(
                        "Entry Date       : "
                                + series.getBar(entryIndex)
                                .getEndTime()
                );

                System.out.printf(
                        "Entry Price      : %.2f%n",
                        entryPrice
                );

                System.out.println(
                        "Current Date     : "
                                + series.getBar(currentIndex)
                                .getEndTime()
                );

                System.out.printf(
                        "Current Price    : %.2f%n",
                        currentPrice
                );

                System.out.printf(
                        "Unrealized P/L   : %.2f%n",
                        unrealizedProfit
                );

                System.out.println(
                        "Holding Bars     : "
                                + holdingBars
                );

                System.out.println("=========================");
            }
        }

        System.out.println("=========================");
    }
}