package com.rithish.trading.service.downloader;

import com.rithish.trading.contracts.AlphaVantageClient;
import com.rithish.trading.exceptions.HistoricalDataDownloadException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class RestClientAlphaVantageClient
        implements AlphaVantageClient {

    private static final String BASE_URL =
            "https://www.alphavantage.co/query";

    private final RestClient restClient;

    public RestClientAlphaVantageClient() {

        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    @Override
    public String getDailyTimeSeries(
            String symbol,
            String period,
            String apiKey) {

        try {

            log.debug(
                    "Calling Alpha Vantage for symbol: {}",
                    symbol
                    );
        String response =
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam(
                                        "function",
                                        "TIME_SERIES_DAILY"
                                )
                                .queryParam(
                                        "symbol",
                                        symbol
                                )
                                .queryParam(
                                        "outputsize",
                                        "compact"
                                )
                                .queryParam(
                                        "apikey",
                                        apiKey
                                )
                                .build())
                        .retrieve()
                        .body(String.class);

        log.info(
                "Alpha Vantage response for {}: {}",
                symbol,
                response
        );

            if (response == null
                    || response.isBlank()) {

                throw new HistoricalDataDownloadException(
                        "Empty response received from Alpha Vantage"
                );
            }

            return response;

        } catch (HistoricalDataDownloadException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Alpha Vantage HTTP request failed for {}",
                    symbol,
                    e
            );

            throw new HistoricalDataDownloadException(
                    "Failed to call Alpha Vantage for "
                            + symbol,
                    e
            );
        }
    }
}