package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import com.rithish.trading.loader.ApiMarketDataLoader;
import com.rithish.trading.loader.CsvMarketDataLoader;
import com.rithish.trading.loader.FallbackMarketDataLoader;

class FallbackMarketDataLoaderTest {

    private ApiMarketDataLoader apiMarketDataLoader;
    private CsvMarketDataLoader csvMarketDataLoader;

    private FallbackMarketDataLoader fallbackLoader;

    @BeforeEach
    void setUp() {

        apiMarketDataLoader =
                mock(ApiMarketDataLoader.class);

        csvMarketDataLoader =
                mock(CsvMarketDataLoader.class);

        fallbackLoader =
                new FallbackMarketDataLoader(
                        apiMarketDataLoader,
                        csvMarketDataLoader
                );
    }

    @Test
    void shouldRejectNullSymbol() {

        assertThrows(
                IllegalArgumentException.class,
                () -> fallbackLoader.loadSeries(null)
        );

        verifyNoInteractions(
                apiMarketDataLoader,
                csvMarketDataLoader
        );
    }

    @Test
    void shouldRejectBlankSymbol() {

        assertThrows(
                IllegalArgumentException.class,
                () -> fallbackLoader.loadSeries("   ")
        );

        verifyNoInteractions(
                apiMarketDataLoader,
                csvMarketDataLoader
        );
    }

    @Test
    void shouldReturnApiDataWhenApiSucceeds() {

        BarSeries apiSeries =
                mock(BarSeries.class);

        when(apiSeries.isEmpty())
                .thenReturn(false);

        when(apiSeries.getBarCount())
                .thenReturn(100);

        when(apiMarketDataLoader.loadSeries("TCS"))
                .thenReturn(apiSeries);

        BarSeries result =
                fallbackLoader.loadSeries("tcs");

        assertSame(
                apiSeries,
                result
        );

        verify(apiMarketDataLoader)
                .loadSeries("TCS");

        verifyNoInteractions(
                csvMarketDataLoader
        );
    }

    @Test
    void shouldFallbackToCsvWhenApiReturnsNull() {

        BarSeries csvSeries =
                mock(BarSeries.class);

        when(csvSeries.isEmpty())
                .thenReturn(false);

        when(csvSeries.getBarCount())
                .thenReturn(100);

        when(apiMarketDataLoader.loadSeries("TCS"))
                .thenReturn(null);

        when(csvMarketDataLoader.loadSeries("TCS"))
                .thenReturn(csvSeries);

        BarSeries result =
                fallbackLoader.loadSeries("tcs");

        assertSame(
                csvSeries,
                result
        );

        verify(apiMarketDataLoader)
                .loadSeries("TCS");

        verify(csvMarketDataLoader)
                .loadSeries("TCS");
    }

    @Test
    void shouldFallbackToCsvWhenApiReturnsEmptySeries() {

        BarSeries apiSeries =
                mock(BarSeries.class);

        BarSeries csvSeries =
                mock(BarSeries.class);

        when(apiSeries.isEmpty())
                .thenReturn(true);

        when(csvSeries.isEmpty())
                .thenReturn(false);

        when(csvSeries.getBarCount())
                .thenReturn(100);

        when(apiMarketDataLoader.loadSeries("TCS"))
                .thenReturn(apiSeries);

        when(csvMarketDataLoader.loadSeries("TCS"))
                .thenReturn(csvSeries);

        BarSeries result =
                fallbackLoader.loadSeries("TCS");

        assertSame(
                csvSeries,
                result
        );

        verify(apiMarketDataLoader)
                .loadSeries("TCS");

        verify(csvMarketDataLoader)
                .loadSeries("TCS");
    }

    @Test
    void shouldFallbackToCsvWhenApiThrowsException() {

        BarSeries csvSeries =
                mock(BarSeries.class);

        when(apiMarketDataLoader.loadSeries("TCS"))
                .thenThrow(
                        new RuntimeException(
                                "API unavailable"
                        )
                );

        when(csvSeries.isEmpty())
                .thenReturn(false);

        when(csvSeries.getBarCount())
                .thenReturn(100);

        when(csvMarketDataLoader.loadSeries("TCS"))
                .thenReturn(csvSeries);

        BarSeries result =
                fallbackLoader.loadSeries("TCS");

        assertSame(
                csvSeries,
                result
        );

        verify(apiMarketDataLoader)
                .loadSeries("TCS");

        verify(csvMarketDataLoader)
                .loadSeries("TCS");
    }

    @Test
    void shouldThrowExceptionWhenCsvAlsoFails() {

        when(apiMarketDataLoader.loadSeries("TCS"))
                .thenThrow(
                        new RuntimeException(
                                "API unavailable"
                        )
                );

        when(csvMarketDataLoader.loadSeries("TCS"))
                .thenThrow(
                        new RuntimeException(
                                "CSV unavailable"
                        )
                );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> fallbackLoader.loadSeries("TCS")
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "Unable to load historical data"
                        )
        );

        verify(apiMarketDataLoader)
                .loadSeries("TCS");

        verify(csvMarketDataLoader)
                .loadSeries("TCS");
    }

    @Test
    void shouldThrowExceptionWhenBothSourcesReturnEmpty() {

        BarSeries apiSeries =
                mock(BarSeries.class);

        BarSeries csvSeries =
                mock(BarSeries.class);

        when(apiSeries.isEmpty())
                .thenReturn(true);

        when(csvSeries.isEmpty())
                .thenReturn(true);

        when(apiMarketDataLoader.loadSeries("TCS"))
                .thenReturn(apiSeries);

        when(csvMarketDataLoader.loadSeries("TCS"))
                .thenReturn(csvSeries);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> fallbackLoader.loadSeries("TCS")
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                "No historical data available"
                        )
        );

        verify(apiMarketDataLoader)
                .loadSeries("TCS");

        verify(csvMarketDataLoader)
                .loadSeries("TCS");
    }
}