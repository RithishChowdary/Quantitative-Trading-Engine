package com.rithish.trading.contracts;

public interface AlphaVantageClient {

    String getDailyTimeSeries(
            String symbol,
            String period,
            String apiKey
    );
}