package com.rithish.trading.loader;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import com.rithish.trading.contracts.MarketDataLoader;

import lombok.extern.slf4j.Slf4j;

@Component
@Primary
@Slf4j
public class FallbackMarketDataLoader
        implements MarketDataLoader {

    private final ApiMarketDataLoader apiMarketDataLoader;
    private final CsvMarketDataLoader csvMarketDataLoader;

    public FallbackMarketDataLoader(
            ApiMarketDataLoader apiMarketDataLoader,
            CsvMarketDataLoader csvMarketDataLoader) {

        this.apiMarketDataLoader = apiMarketDataLoader;
        this.csvMarketDataLoader = csvMarketDataLoader;
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

        /*
         * 1. Try external API first.
         */
        try {

            log.info(
                    "Attempting to load {} from Alpha Vantage API",
                    normalizedSymbol
            );

            BarSeries apiSeries =
                    apiMarketDataLoader.loadSeries(
                            normalizedSymbol
                    );

            if (apiSeries != null
                    && !apiSeries.isEmpty()) {

                log.info(
                        "Using API data for {}. Bars: {}",
                        normalizedSymbol,
                        apiSeries.getBarCount()
                );

                return apiSeries;
            }

            log.warn(
                    "API returned no data for {}. Falling back to CSV.",
                    normalizedSymbol
            );

        } catch (Exception e) {

            log.warn(
                    "API data unavailable for {}. "
                            + "Falling back to CSV. Reason: {}",
                    normalizedSymbol,
                    e.getMessage()
            );
        }

        /*
         * 2. API failed → use CSV.
         */
        try {

            log.info(
                    "Loading {} from CSV fallback",
                    normalizedSymbol
            );

            BarSeries csvSeries =
                    csvMarketDataLoader.loadSeries(
                            normalizedSymbol
                    );

            if (csvSeries != null
                    && !csvSeries.isEmpty()) {

                log.info(
                        "Using CSV data for {}. Bars: {}",
                        normalizedSymbol,
                        csvSeries.getBarCount()
                );

                return csvSeries;
            }

        } catch (Exception e) {

            log.error(
                    "CSV fallback also failed for {}",
                    normalizedSymbol,
                    e
            );

            throw new IllegalStateException(
                    "Unable to load historical data for "
                            + normalizedSymbol
                            + " from API or CSV",
                    e
            );
        }

        throw new IllegalStateException(
                "No historical data available for "
                        + normalizedSymbol
                        + " from API or CSV"
        );
    }
}