package com.rithish.trading.service.downloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rithish.trading.contracts.HistoricalDataDownloader;
import com.rithish.trading.dto.api.HistoricalResponse;
import com.rithish.trading.exceptions.HistoricalDataDownloadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndianApiHistoricalDownloader
        implements HistoricalDataDownloader {

    private static final String BASE_URL =
            "https://stock.indianapi.in/historical_data";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Override
    public HistoricalResponse download(
            String symbol,
            String period,
            String apiKey) {

        validateInput(symbol, period, apiKey);

        String url = buildUrl(symbol, period);

        log.info(
                "Downloading historical data for symbol: {}, period: {}",
                symbol,
                period
        );

        try {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("x-api-key", apiKey)
                            .header("Accept", "application/json")
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {

                log.error(
                        "Indian API returned HTTP status {} for symbol {}",
                        response.statusCode(),
                        symbol
                );

                throw new HistoricalDataDownloadException(
                        "Indian API returned HTTP status: "
                                + response.statusCode()
                );
            }

            log.info(
                    "Historical data downloaded successfully for {}",
                    symbol
            );

            return objectMapper.readValue(
                    response.body(),
                    HistoricalResponse.class
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new HistoricalDataDownloadException(
                    "Historical data download was interrupted",
                    e
            );

        } catch (IOException e) {

            throw new HistoricalDataDownloadException(
                    "Failed to download historical data for "
                            + symbol,
                    e
            );
        }
    }

    private String buildUrl(
            String symbol,
            String period) {

        return BASE_URL
                + "?stock_name="
                + encode(symbol)
                + "&period="
                + encode(period)
                + "&filter=price";
    }

    private String encode(String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }

    private void validateInput(
            String symbol,
            String period,
            String apiKey) {

        if (symbol == null || symbol.isBlank()) {

            throw new IllegalArgumentException(
                    "Symbol must not be empty"
            );
        }

        if (period == null || period.isBlank()) {

            throw new IllegalArgumentException(
                    "Period must not be empty"
            );
        }

        if (apiKey == null || apiKey.isBlank()) {

            throw new IllegalArgumentException(
                    "API key must not be empty"
            );
        }
    }
}