package com.rithish.trading.service.impl;

import com.rithish.trading.contracts.HistoricalDataDownloader;
import com.rithish.trading.dto.api.HistoricalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoricalDataService {

    private final HistoricalDataDownloader downloader;

    public HistoricalResponse downloadHistoricalData(
            String symbol,
            String period,
            String apiKey) {

        log.info(
                "Requesting historical data for symbol: {}, period: {}",
                symbol,
                period
        );

        return downloader.download(
                symbol,
                period,
                apiKey
        );
    }
}