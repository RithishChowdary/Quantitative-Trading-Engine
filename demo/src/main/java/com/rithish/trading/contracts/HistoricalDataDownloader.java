package com.rithish.trading.contracts;

import com.rithish.trading.dto.api.HistoricalResponse;

public interface HistoricalDataDownloader {

    HistoricalResponse download(
            String symbol,
            String period,
            String apiKey
    );
}