package com.rithish.trading.service.downloader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rithish.trading.contracts.AlphaVantageClient;
import com.rithish.trading.contracts.HistoricalDataDownloader;
import com.rithish.trading.dto.api.Dataset;
import com.rithish.trading.dto.api.HistoricalResponse;
import com.rithish.trading.dto.historical.HistoricalCandle;
import com.rithish.trading.exceptions.HistoricalDataDownloadException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
public class AlphaVantageHistoricalDataDownloader
        implements HistoricalDataDownloader {

    private static final String[] EXCHANGES = {
            "BSE",
            "NSE"
    };

    private static final LocalTime MARKET_CLOSE =
            LocalTime.of(15, 30);

    private final AlphaVantageClient client;
    private final ObjectMapper objectMapper;

    public AlphaVantageHistoricalDataDownloader(
            AlphaVantageClient client,
            ObjectMapper objectMapper) {

        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
   public HistoricalResponse download(
        String symbol,
        String interval,
        String apiKey){

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Symbol must not be empty"
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(
                    "API key must not be empty"
            );
        }

        String normalizedSymbol =
                symbol.trim().toUpperCase();

        for (String exchange : EXCHANGES) {

            String exchangeSymbol =
                    normalizedSymbol + "." + exchange;

            try {

                log.info(
                        "Trying Alpha Vantage symbol: {}",
                        exchangeSymbol
                );

                String response =
                        requestHistoricalData(
                                exchangeSymbol,
                                interval,
                                apiKey
                        );

                JsonNode root =
                        objectMapper.readTree(response);

                /*
                 * Alpha Vantage API rate-limit response.
                 */
                if (root.has("Note") || root.has("Information")) {

                    throw new HistoricalDataDownloadException(
                            "You have requested historical data for a symbol that is not available in the data source."
                    );
                }

                /*
                 * Symbol not supported / invalid.
                 * Try the next exchange.
                 */
                if (root.has("Error Message")) {

                    log.warn(
                            "Symbol {} not available: {}",
                            exchangeSymbol,
                            root.get("Error Message").asText()
                    );

                    continue;
                }

                JsonNode timeSeries =
                        root.get("Time Series (Daily)");

                if (timeSeries == null
                        || !timeSeries.isObject()
                        || timeSeries.isEmpty()) {

                    log.warn(
                            "No daily data found for {}",
                            exchangeSymbol
                    );

                    continue;
                }

                /*
                 * Convert API JSON into HistoricalCandle objects.
                 */
                List<HistoricalCandle> candles =
                        parseCandles(timeSeries);

                if (candles.isEmpty()) {

                    log.warn(
                            "No valid candles found for {}",
                            exchangeSymbol
                    );

                    continue;
                }

                Dataset dataset =
                        new Dataset(
                                normalizedSymbol,
                                exchange,
                                "DAILY"
                        );

                log.info(
                        "Successfully downloaded {} candles for {}",
                        candles.size(),
                        exchangeSymbol
                );

                return new HistoricalResponse(
                        List.of(dataset),
                        candles
                );

            } catch (HistoricalDataDownloadException e) {

                /*
                 * API limit or download failure should
                 * propagate to FallbackMarketDataLoader.
                 */
                throw e;

            } catch (Exception e) {

                /*
                 * Try the next exchange if this exchange fails.
                 */
                log.warn(
                        "Failed to download {}. Trying next exchange.",
                        exchangeSymbol,
                        e
                );
            }
        }

        /*
         * Neither NSE nor BSE produced valid data.
         */
        throw new HistoricalDataDownloadException(
                "Historical data not available for "
                        + normalizedSymbol
                        + " on NSE or BSE"
        );
    }

    /**
     * Delegates the actual HTTP request to AlphaVantageClient.
     *
     * The downloader is responsible for parsing the response,
     * while the client is responsible for communication.
     */
   private String requestHistoricalData(
        String symbol,
        String interval,
        String apiKey) {

        return client.getDailyTimeSeries(
                symbol,
                interval,
                apiKey
        );
    }

    /**
     * Converts Alpha Vantage daily time-series JSON
     * into HistoricalCandle objects.
     */
    private List<HistoricalCandle> parseCandles(
            JsonNode timeSeries) {

        List<HistoricalCandle> candles =
                new ArrayList<>();

        timeSeries.properties()
                .forEach(entry -> {

                    try {

                        LocalDate date =
                                LocalDate.parse(
                                        entry.getKey()
                                );

                        JsonNode data =
                                entry.getValue();

                        double open =
                                parsePositive(
                                        data,
                                        "1. open"
                                );

                        double high =
                                parsePositive(
                                        data,
                                        "2. high"
                                );

                        double low =
                                parsePositive(
                                        data,
                                        "3. low"
                                );

                        double close =
                                parsePositive(
                                        data,
                                        "4. close"
                                );

                        double volume =
                                parseNonNegative(
                                        data,
                                        "5. volume"
                                );

                        /*
                         * Validate OHLC relationship.
                         */
                        if (high < Math.max(open, close)
                                || low > Math.min(open, close)
                                || low > high) {

                            log.warn(
                                    "Skipping invalid OHLC data for {}",
                                    date
                            );

                            return;
                        }

                        candles.add(
                                new HistoricalCandle(
                                        LocalDateTime.of(
                                                date,
                                                MARKET_CLOSE
                                        ),
                                        open,
                                        high,
                                        low,
                                        close,
                                        volume
                                )
                        );

                    } catch (Exception e) {

                        log.warn(
                                "Skipping invalid historical candle: {}",
                                entry.getKey()
                        );
                    }
                });

        /*
         * Alpha Vantage normally returns newest-first.
         * TA4J backtesting should receive oldest-first.
         */
        candles.sort(
                Comparator.comparing(
                        HistoricalCandle::getTimestamp
                )
        );

        return candles;
    }

    private double parsePositive(
            JsonNode data,
            String field) {

        if (!data.has(field)) {

            throw new IllegalArgumentException(
                    "Missing field: " + field
            );
        }

        double value =
                data.get(field).asDouble();

        if (!Double.isFinite(value)
                || value <= 0) {

            throw new IllegalArgumentException(
                    field
                            + " must be a positive finite number"
            );
        }

        return value;
    }

    private double parseNonNegative(
            JsonNode data,
            String field) {

        if (!data.has(field)) {

            throw new IllegalArgumentException(
                    "Missing field: " + field
            );
        }

        double value =
                data.get(field).asDouble();

        if (!Double.isFinite(value)
                || value < 0) {

            throw new IllegalArgumentException(
                    field
                            + " must be a non-negative finite number"
            );
        }

        return value;
    }
}