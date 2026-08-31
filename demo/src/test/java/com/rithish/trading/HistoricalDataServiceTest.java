package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import com.rithish.trading.config.AlphaVantageProperties;
import com.rithish.trading.contracts.HistoricalDataDownloader;
import com.rithish.trading.dto.api.HistoricalResponse;
import com.rithish.trading.service.impl.HistoricalDataService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HistoricalDataServiceTest {

    private HistoricalDataDownloader downloader;
    private AlphaVantageProperties properties;

    private HistoricalDataService service;

    @BeforeEach
    void setUp() {

        downloader =
                mock(HistoricalDataDownloader.class);

        properties =
                mock(AlphaVantageProperties.class);

        service =
                new HistoricalDataService(
                        downloader,
                        properties
                );
    }

    @Test
    void shouldDownloadHistoricalData() {

        HistoricalResponse expected =
                mock(HistoricalResponse.class);

        when(properties.getApiKey())
                .thenReturn("test-api-key");

        when(downloader.download(
                "TCS",
                "compact",
                "test-api-key"
        )).thenReturn(expected);

        HistoricalResponse result =
                service.downloadHistoricalData(
                        "TCS",
                        "compact"
                );

        assertSame(
                expected,
                result
        );

        verify(properties)
                .getApiKey();

        verify(downloader)
                .download(
                        "TCS",
                        "compact",
                        "test-api-key"
                );
    }
}