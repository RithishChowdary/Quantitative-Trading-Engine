package com.rithish.trading.report;

/**
 * Represents calculated performance metrics of a backtest.
 *
 * This class is a data holder.
 * Calculation logic remains outside this class.
 */
public class PerformanceMetrics {

    private final int totalTrades;
    private final int winningTrades;
    private final int losingTrades;

    private final double winRate;

    /**
     * Gross profit from successful trades only.
     */
    private final double totalProfit;

    /**
     * Gross loss from losing trades only.
     * Stored as a positive value for reporting.
     */
    private final double totalLoss;

    /**
     * Net earnings after subtracting total loss from total profit.
     */
    private final double totalEarnings;

    private final double averageProfit;
    private final double maximumDrawdown;
    private final double profitFactor;

    public PerformanceMetrics(
            int totalTrades,
            int winningTrades,
            int losingTrades,
            double winRate,
            double totalProfit,
            double totalLoss,
            double totalEarnings,
            double averageProfit,
            double maximumDrawdown,
            double profitFactor) {

        this.totalTrades = totalTrades;
        this.winningTrades = winningTrades;
        this.losingTrades = losingTrades;
        this.winRate = winRate;
        this.totalProfit = totalProfit;
        this.totalLoss = totalLoss;
        this.totalEarnings = totalEarnings;
        this.averageProfit = averageProfit;
        this.maximumDrawdown = maximumDrawdown;
        this.profitFactor = profitFactor;
    }

    public int getTotalTrades() {
        return totalTrades;
    }

    public int getWinningTrades() {
        return winningTrades;
    }

    public int getLosingTrades() {
        return losingTrades;
    }

    public double getWinRate() {
        return winRate;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public double getTotalLoss() {
        return totalLoss;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }

    public double getAverageProfit() {
        return averageProfit;
    }

    public double getMaximumDrawdown() {
        return maximumDrawdown;
    }

    public double getProfitFactor() {
        return profitFactor;
    }
}