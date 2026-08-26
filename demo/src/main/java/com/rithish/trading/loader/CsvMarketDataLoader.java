package com.rithish.trading.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import com.rithish.trading.contracts.MarketDataLoader;
import com.rithish.trading.dto.historical.HistoricalCandle;

import lombok.extern.slf4j.Slf4j;

/**
 * Loads daily OHLCV data from classpath CSV files.
 *
 * <p>Malformed/event rows are skipped so raw market-data exports containing
 * dividend or corporate-action rows do not break the backtest.</p>
 */
@Slf4j
public class CsvMarketDataLoader implements MarketDataLoader {

    private static final Duration BAR_DURATION =
            Duration.ofDays(1);

    private static final ZoneId MARKET_ZONE =
            ZoneId.of("Asia/Kolkata");

    private static final LocalTime MARKET_CLOSE =
            LocalTime.of(15, 30);

    @Override
    public BarSeries loadSeries(String symbol) {

        String normalizedSymbol = normalizeSymbol(symbol);

        BarSeries series =
                new BaseBarSeriesBuilder()
                        .withName(normalizedSymbol)
                        .build();

        String resourcePath =
                "historical/NSE/" + normalizedSymbol + ".csv";

        InputStream inputStream =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IllegalArgumentException(
                    "Historical data file not found: " + resourcePath
            );
        }

        Set<LocalDate> loadedDates = new HashSet<>();
        LocalDate previousDate = null;
        int skippedRows = 0;

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     inputStream,
                                     StandardCharsets.UTF_8))) {

            String header = reader.readLine();

            if (header == null || header.isBlank()) {
                throw new IllegalArgumentException(
                        "Historical data file is empty: " + resourcePath
                );
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                try {
                    HistoricalCandle candle = parseCandle(line);

                    LocalDate date = candle.getTimestamp().toLocalDate();

                    if (previousDate != null && date.isBefore(previousDate)) {
                        throw new IllegalArgumentException(
                                "CSV dates must be sorted oldest to newest"
                        );
                    }

                    if (!loadedDates.add(date)) {
                        log.warn(
                                "Skipping duplicate date {} at line {} in {}",
                                date,
                                lineNumber,
                                resourcePath
                        );
                        skippedRows++;
                        continue;
                    }

                    addCandleToSeries(series, candle);
                    previousDate = date;

                } catch (IllegalArgumentException e) {
                    skippedRows++;
                    log.warn(
                            "Skipping invalid CSV row at line {} in {}: {}",
                            lineNumber,
                            resourcePath,
                            e.getMessage()
                    );
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load historical data for " + normalizedSymbol,
                    e
            );
        }

        if (series.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid OHLCV rows found in: " + resourcePath
            );
        }

        if (skippedRows > 0) {
            log.info(
                    "Loaded {} bars for {}. Skipped {} invalid/event rows.",
                    series.getBarCount(),
                    normalizedSymbol,
                    skippedRows
            );
        } else {
            log.info(
                    "Loaded {} bars for {}.",
                    series.getBarCount(),
                    normalizedSymbol
            );
        }

        return series;
    }

    private HistoricalCandle parseCandle(String line) {

        String[] data = line.split(",", -1);

        if (data.length < 6) {
            throw new IllegalArgumentException(
                    "Expected Date,Open,High,Low,Close,Volume"
            );
        }

        LocalDate date = LocalDate.parse(data[0].trim());

        double open = parsePositiveFinite(data[1], "open");
        double high = parsePositiveFinite(data[2], "high");
        double low = parsePositiveFinite(data[3], "low");
        double close = parsePositiveFinite(data[4], "close");
        double volume = parseNonNegativeFinite(data[5], "volume");

        if (high < Math.max(open, close)
                || low > Math.min(open, close)
                || low > high) {
            throw new IllegalArgumentException(
                    "Invalid OHLC relationship"
            );
        }

        return new HistoricalCandle(
                LocalDateTime.of(date, MARKET_CLOSE),
                open,
                high,
                low,
                close,
                volume
        );
    }

    private double parsePositiveFinite(
            String value,
            String field) {

        double parsed = Double.parseDouble(value.trim());

        if (!Double.isFinite(parsed) || parsed <= 0) {
            throw new IllegalArgumentException(
                    field + " must be a positive finite number"
            );
        }

        return parsed;
    }

    private double parseNonNegativeFinite(
            String value,
            String field) {

        double parsed = Double.parseDouble(value.trim());

        if (!Double.isFinite(parsed) || parsed < 0) {
            throw new IllegalArgumentException(
                    field + " must be a non-negative finite number"
            );
        }

        return parsed;
    }

    private void addCandleToSeries(
            BarSeries series,
            HistoricalCandle candle) {

        series.barBuilder()
                .timePeriod(BAR_DURATION)
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

    private String normalizeSymbol(String symbol) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Symbol must not be empty"
            );
        }

        String normalized = symbol.trim().toUpperCase();

        if (!normalized.matches("[A-Z0-9._-]+")) {
            throw new IllegalArgumentException(
                    "Invalid symbol: " + symbol
            );
        }

        return normalized;
    }
}
