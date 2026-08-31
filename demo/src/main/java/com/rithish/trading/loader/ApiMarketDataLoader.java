package com.rithish.trading.loader;

import java.time.ZoneId;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import com.rithish.trading.contracts.MarketDataLoader;
import com.rithish.trading.dto.api.HistoricalResponse;
import com.rithish.trading.dto.historical.HistoricalCandle;
import com.rithish.trading.service.impl.HistoricalDataService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApiMarketDataLoader implements MarketDataLoader {

    private static final ZoneId MARKET_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final HistoricalDataService historicalDataService;

    public ApiMarketDataLoader(
            HistoricalDataService historicalDataService) {

        this.historicalDataService =
                Objects.requireNonNull(
                        historicalDataService,
                        "HistoricalDataService must not be null"
                );
    }

    @Override
    public BarSeries loadSeries(String symbol) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Symbol must not be empty"
            );
        }

        String normalizedSymbol =
                symbol.trim().toUpperCase();

        log.info(
                "Loading market data from API for {}",
                normalizedSymbol
        );
        HistoricalResponse response =
                historicalDataService.downloadHistoricalData(
                        normalizedSymbol,
                        "compact"
                );

        if (response == null
                || response.getCandles() == null
                || response.getCandles().isEmpty()) {

            throw new IllegalArgumentException(
                    "No historical candles received for "
                            + normalizedSymbol
            );
        }

        BarSeries series =
                new BaseBarSeriesBuilder()
                        .withName(normalizedSymbol)
                        .build();

        for (HistoricalCandle candle :
                response.getCandles()) {

            if (candle == null) {
                continue;
            }

            series.barBuilder()
                    .timePeriod(
                            java.time.Duration.ofDays(1)
                    )
                    .endTime(
                            candle.getTimestamp()
                                    .atZone(MARKET_ZONE)
                                    .toInstant()
                    )
                    .openPrice(candle.getOpen())
                    .highPrice(candle.getHigh())
                    .lowPrice(candle.getLow())
                    .closePrice(candle.getClose())
                    .volume(candle.getVolume())
                    .add();
        }

        if (series.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unable to create BarSeries for "
                            + normalizedSymbol
            );
        }

        log.info(
                "API market data loaded successfully. "
                        + "Symbol: {}, bars: {}",
                normalizedSymbol,
                series.getBarCount()
        );

        return series;
    }
}