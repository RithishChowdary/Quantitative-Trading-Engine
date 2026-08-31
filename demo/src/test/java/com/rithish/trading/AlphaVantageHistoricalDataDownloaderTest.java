package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rithish.trading.contracts.AlphaVantageClient;
import com.rithish.trading.dto.api.HistoricalResponse;
import com.rithish.trading.exceptions.HistoricalDataDownloadException;
import com.rithish.trading.service.downloader.AlphaVantageHistoricalDataDownloader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlphaVantageHistoricalDataDownloaderTest {

    private AlphaVantageClient client;

    private AlphaVantageHistoricalDataDownloader downloader;

    @BeforeEach
    void setUp() {

        client = mock(AlphaVantageClient.class);

        downloader =
                new AlphaVantageHistoricalDataDownloader(
                        client,
                        new ObjectMapper()
                );
    }

    @Test
    void shouldRejectNullSymbol() {

        assertThrows(
                IllegalArgumentException.class,
                () -> downloader.download(
                        null,
                        "compact",
                        "test-api-key"
                )
        );
    }

    @Test
    void shouldRejectBlankSymbol() {

        assertThrows(
                IllegalArgumentException.class,
                () -> downloader.download(
                        "   ",
                        "compact",
                        "test-api-key"
                )
        );
    }

    @Test
    void shouldRejectNullApiKey() {

        assertThrows(
                IllegalArgumentException.class,
                () -> downloader.download(
                        "TCS",
                        "compact",
                        null
                )
        );
    }

    @Test
    void shouldRejectBlankApiKey() {

        assertThrows(
                IllegalArgumentException.class,
                () -> downloader.download(
                        "TCS",
                        "compact",
                        "   "
                )
        );
    }

    @Test
    void shouldDownloadHistoricalDataSuccessfully() {

        String response = """
                {
                  "Meta Data": {
                    "1. Information": "Daily Prices",
                    "2. Symbol": "TCS.NSE"
                  },
                  "Time Series (Daily)": {
                    "2026-08-28": {
                      "1. open": "3500.00",
                      "2. high": "3550.00",
                      "3. low": "3480.00",
                      "4. close": "3520.00",
                      "5. volume": "100000"
                    },
                    "2026-08-27": {
                      "1. open": "3450.00",
                      "2. high": "3510.00",
                      "3. low": "3430.00",
                      "4. close": "3500.00",
                      "5. volume": "120000"
                    }
                  }
                }
                """;

        when(client.getDailyTimeSeries(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(response);

        HistoricalResponse result =
                downloader.download(
                        "TCS",
                        "compact",
                        "test-api-key"
                );

        assertNotNull(result);

        assertNotNull(result.getDatasets());
        assertEquals(
                1,
                result.getDatasets().size()
        );

        assertNotNull(result.getCandles());

        assertEquals(
                2,
                result.getCandles().size()
        );

        assertEquals(
                "TCS",
                result.getDatasets()
                        .get(0)
                        .getSymbol()
        );

        assertEquals(
                "BSE",
                result.getDatasets()
                        .get(0)
                        .getExchange()
        );

        verify(client, times(1))
                .getDailyTimeSeries(
                        "TCS.BSE",
                        "compact",
                        "test-api-key"
                );
    }
        @Test
        void shouldTryNseWhenBseHasNoData() {

        String emptyResponse = """
                {
                "Error Message": "Invalid API call"
                }
                """;

        String nseResponse = """
                {
                "Meta Data": {
                        "1. Information": "Daily Prices",
                        "2. Symbol": "TCS.NSE"
                },
                "Time Series (Daily)": {
                        "2026-08-28": {
                        "1. open": "3500.00",
                        "2. high": "3550.00",
                        "3. low": "3480.00",
                        "4. close": "3520.00",
                        "5. volume": "100000"
                        }
                }
                }
                """;

        when(client.getDailyTimeSeries(
                "TCS.BSE",
                "compact",
                "test-api-key"
        )).thenReturn(emptyResponse);

        when(client.getDailyTimeSeries(
                "TCS.NSE",
                "compact",
                "test-api-key"
        )).thenReturn(nseResponse);

        HistoricalResponse result =
                downloader.download(
                        "TCS",
                        "compact",
                        "test-api-key"
                );

        assertNotNull(result);

        assertEquals(
                1,
                result.getCandles().size()
        );

        assertEquals(
                "NSE",
                result.getDatasets()
                        .get(0)
                        .getExchange()
        );

        verify(client)
                .getDailyTimeSeries(
                        "TCS.BSE",
                        "compact",
                        "test-api-key"
                );

        verify(client)
                .getDailyTimeSeries(
                        "TCS.NSE",
                        "compact",
                        "test-api-key"
                );
        }
    @Test
    void shouldThrowExceptionWhenBothExchangesFail() {

        String invalidResponse = """
                {
                  "Error Message": "Invalid API call"
                }
                """;

        when(client.getDailyTimeSeries(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(invalidResponse);

        assertThrows(
                HistoricalDataDownloadException.class,
                () -> downloader.download(
                        "TCS",
                        "compact",
                        "test-api-key"
                )
        );

        verify(client)
        .getDailyTimeSeries(
                "TCS.BSE",
                "compact",
                "test-api-key"
        );

        verify(client)
                .getDailyTimeSeries(
                        "TCS.NSE",
                        "compact",
                        "test-api-key"
                );
    }

    @Test
    void shouldThrowCustomUnavailableMessageForApiLimitResponse() {

        String limitResponse = """
                {
                  "Note": "API call frequency limit reached."
                }
                """;

        when(client.getDailyTimeSeries(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(limitResponse);

        HistoricalDataDownloadException exception = assertThrows(
                HistoricalDataDownloadException.class,
                () -> downloader.download(
                        "TCS",
                        "compact",
                        "test-api-key"
                )
        );

        assertEquals(
                "You have requested historical data for a symbol that is not available in the data source.",
                exception.getMessage()
        );
    }
}