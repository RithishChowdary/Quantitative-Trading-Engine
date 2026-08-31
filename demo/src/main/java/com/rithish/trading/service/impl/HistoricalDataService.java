package com.rithish.trading.service.impl;

import com.rithish.trading.config.AlphaVantageProperties;
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
    private final AlphaVantageProperties alphaVantageProperties;

        public HistoricalResponse downloadHistoricalData(
                String symbol,
                String interval) {

        log.info(
                "Requesting historical data for symbol: {}, interval: {}",
                symbol,
                interval
        );

        return downloader.download(
                symbol,
                interval,
                alphaVantageProperties.getApiKey()
        );
        }
}