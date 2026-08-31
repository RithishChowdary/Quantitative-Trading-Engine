package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import com.rithish.trading.loader.CsvMarketDataLoader;

class CsvMarketDataLoaderTest {

    private final CsvMarketDataLoader loader =
            new CsvMarketDataLoader();

    @Test
    void shouldLoadExistingSymbol() {

        BarSeries series = loader.loadSeries("TCS");

        assertNotNull(series);
        assertFalse(series.isEmpty());
        assertEquals("TCS", series.getName());
    }

    @Test
    void shouldNormalizeSymbolToUpperCase() {

        BarSeries series = loader.loadSeries("tcs");

        assertNotNull(series);
        assertFalse(series.isEmpty());
        assertEquals("TCS", series.getName());
    }

    @Test
    void shouldRejectNullSymbol() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.loadSeries(null)
        );
    }

    @Test
    void shouldRejectBlankSymbol() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.loadSeries("   ")
        );
    }

    @Test
    void shouldRejectInvalidSymbol() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.loadSeries("TCS@123")
        );
    }

    @Test
    void shouldRejectMissingHistoricalDataFile() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.loadSeries("UNKNOWN_SYMBOL")
        );
    }
}