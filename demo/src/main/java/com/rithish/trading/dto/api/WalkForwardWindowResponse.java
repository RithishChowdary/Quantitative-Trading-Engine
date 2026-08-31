package com.rithish.trading.dto.api;

public class WalkForwardWindowResponse {

    private final int windowNumber;

    private final int trainingStart;
    private final int trainingEnd;

    private final int testingStart;
    private final int testingEnd;

    private final int fastEmaPeriod;
    private final int slowEmaPeriod;
    private final int rsiPeriod;

    private final double rsiBuyThreshold;
    private final double rsiSellThreshold;

    private final double testingProfit;

    private final int completedTrades;

    public WalkForwardWindowResponse(
            int windowNumber,
            int trainingStart,
            int trainingEnd,
            int testingStart,
            int testingEnd,
            int fastEmaPeriod,
            int slowEmaPeriod,
            int rsiPeriod,
            double rsiBuyThreshold,
            double rsiSellThreshold,
            double testingProfit,
            int completedTrades) {

        this.windowNumber = windowNumber;
        this.trainingStart = trainingStart;
        this.trainingEnd = trainingEnd;
        this.testingStart = testingStart;
        this.testingEnd = testingEnd;
        this.fastEmaPeriod = fastEmaPeriod;
        this.slowEmaPeriod = slowEmaPeriod;
        this.rsiPeriod = rsiPeriod;
        this.rsiBuyThreshold = rsiBuyThreshold;
        this.rsiSellThreshold = rsiSellThreshold;
        this.testingProfit = testingProfit;
        this.completedTrades = completedTrades;
    }

    public int getWindowNumber() {
        return windowNumber;
    }

    public int getTrainingStart() {
        return trainingStart;
    }

    public int getTrainingEnd() {
        return trainingEnd;
    }

    public int getTestingStart() {
        return testingStart;
    }

    public int getTestingEnd() {
        return testingEnd;
    }

    public int getFastEmaPeriod() {
        return fastEmaPeriod;
    }

    public int getSlowEmaPeriod() {
        return slowEmaPeriod;
    }

    public int getRsiPeriod() {
        return rsiPeriod;
    }

    public double getRsiBuyThreshold() {
        return rsiBuyThreshold;
    }

    public double getRsiSellThreshold() {
        return rsiSellThreshold;
    }

    public double getTestingProfit() {
        return testingProfit;
    }

    public int getCompletedTrades() {
        return completedTrades;
    }
}