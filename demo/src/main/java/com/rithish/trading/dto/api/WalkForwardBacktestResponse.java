package com.rithish.trading.dto.api;

import java.util.List;
import java.util.Objects;

public class WalkForwardBacktestResponse {

    private final String symbol;

    private final int windowsEvaluated;

    private final double totalOutOfSampleProfit;

    private final List<WalkForwardWindowResponse> results;

    public WalkForwardBacktestResponse(
            String symbol,
            int windowsEvaluated,
            double totalOutOfSampleProfit,
            List<WalkForwardWindowResponse> results) {

        this.symbol =
                Objects.requireNonNull(
                        symbol,
                        "Symbol must not be null"
                );

        this.windowsEvaluated = windowsEvaluated;

        this.totalOutOfSampleProfit =
                totalOutOfSampleProfit;

        this.results =
                List.copyOf(
                        Objects.requireNonNull(
                                results,
                                "Results must not be null"
                        )
                );
    }

    public String getSymbol() {
        return symbol;
    }

    public int getWindowsEvaluated() {
        return windowsEvaluated;
    }

    public double getTotalOutOfSampleProfit() {
        return totalOutOfSampleProfit;
    }

    public List<WalkForwardWindowResponse> getResults() {
        return results;
    }
}