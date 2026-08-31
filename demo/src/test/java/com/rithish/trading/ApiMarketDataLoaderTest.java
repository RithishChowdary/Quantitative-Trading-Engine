package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import com.rithish.trading.dto.api.Dataset;
import com.rithish.trading.dto.api.HistoricalResponse;
import com.rithish.trading.dto.historical.HistoricalCandle;
import com.rithish.trading.loader.ApiMarketDataLoader;
import com.rithish.trading.service.impl.HistoricalDataService;

class ApiMarketDataLoaderTest {

    private HistoricalDataService historicalDataService;

    private ApiMarketDataLoader loader;

    @BeforeEach
    void setUp() {

        historicalDataService =
                mock(HistoricalDataService.class);

        loader =
                new ApiMarketDataLoader(
                        historicalDataService
                );
    }

    @Test
    void shouldRejectNullSymbol() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.loadSeries(null)
        );

        verifyNoInteractions(
                historicalDataService
        );
    }

    @Test
    void shouldRejectBlankSymbol() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.loadSeries("   ")
        );

        verifyNoInteractions(
                historicalDataService
        );
    }

    @Test
    void shouldNormalizeSymbolAndLoadData() {

        HistoricalCandle candle =
                new HistoricalCandle(
                        LocalDateTime.of(
                                2026,
                                8,
                                1,
                                15,
                                30
                        ),
                        100.0,
                        105.0,
                        98.0,
                        103.0,
                        10000.0
                );

        HistoricalResponse response =
                new HistoricalResponse(
                        List.of(
                                new Dataset(
                                        "TCS",
                                        "NSE",
                                        "DAILY"
                                )
                        ),
                        List.of(candle)
                );

        when(
                historicalDataService
                        .downloadHistoricalData(
                                "TCS",
                                "compact"
                        )
        ).thenReturn(response);

        BarSeries result =
                loader.loadSeries(" tcs ");

        assertNotNull(result);
        assertEquals(
                "TCS",
                result.getName()
        );
        assertEquals(
                1,
                result.getBarCount()
        );

        verify(
                historicalDataService
        ).downloadHistoricalData(
                "TCS",
                "compact"
        );
    }

    @Test
    void shouldConvertMultipleCandlesToBars() {

        HistoricalCandle candle1 =
                new HistoricalCandle(
                        LocalDateTime.of(
                                2026,
                                8,
                                1,
                                15,
                                30
                        ),
                        100.0,
                        105.0,
                        98.0,
                        103.0,
                        10000.0
                );

        HistoricalCandle candle2 =
                new HistoricalCandle(
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                15,
                                30
                        ),
                        103.0,
                        108.0,
                        101.0,
                        106.0,
                        12000.0
                );

        HistoricalResponse response =
                new HistoricalResponse(
                        List.of(
                                new Dataset(
                                        "TCS",
                                        "NSE",
                                        "DAILY"
                                )
                        ),
                        List.of(
                                candle1,
                                candle2
                        )
                );

        when(
                historicalDataService
                        .downloadHistoricalData(
                                "TCS",
                                "compact"
                        )
        ).thenReturn(response);

        BarSeries result =
                loader.loadSeries("TCS");

        assertEquals(
                2,
                result.getBarCount()
        );

        assertEquals(
                100.0,
                result.getBar(0)
                        .getOpenPrice()
                        .doubleValue()
        );

        assertEquals(
                103.0,
                result.getBar(0)
                        .getClosePrice()
                        .doubleValue()
        );

        assertEquals(
                103.0,
                result.getBar(1)
                        .getOpenPrice()
                        .doubleValue()
        );

        assertEquals(
                106.0,
                result.getBar(1)
                        .getClosePrice()
                        .doubleValue()
        );
    }

    @Test
    void shouldRejectNullResponse() {

        when(
                historicalDataService
                        .downloadHistoricalData(
                                "TCS",
                                "compact"
                        )
        ).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.loadSeries("TCS")
        );
    }

    @Test
    void shouldRejectResponseWithEmptyCandles() {

        HistoricalResponse response =
                new HistoricalResponse(
                        List.of(
                                new Dataset(
                                        "TCS",
                                        "NSE",
                                        "DAILY"
                                )
                        ),
                        List.of()
                );

        when(
                historicalDataService
                        .downloadHistoricalData(
                                "TCS",
                                "compact"
                        )
        ).thenReturn(response);

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.loadSeries("TCS")
        );
    }

    @Test
    void shouldSkipNullCandles() {

        HistoricalCandle candle =
                new HistoricalCandle(
                        LocalDateTime.of(
                                2026,
                                8,
                                1,
                                15,
                                30
                        ),
                        100.0,
                        105.0,
                        98.0,
                        103.0,
                        10000.0
                );

        HistoricalResponse response =
                new HistoricalResponse(
                        List.of(
                                new Dataset(
                                        "TCS",
                                        "NSE",
                                        "DAILY"
                                )
                        ),
                        java.util.Arrays.asList(
                                candle,
                                null
                        )
                );

        when(
                historicalDataService
                        .downloadHistoricalData(
                                "TCS",
                                "compact"
                        )
        ).thenReturn(response);

        BarSeries result =
                loader.loadSeries("TCS");

        assertEquals(
                1,
                result.getBarCount()
        );
    }

    @Test
    void shouldRejectWhenAllCandlesAreNull() {

        HistoricalResponse response =
                new HistoricalResponse(
                        List.of(
                                new Dataset(
                                        "TCS",
                                        "NSE",
                                        "DAILY"
                                )
                        ),
                        java.util.Arrays.asList(
                                null,
                                null
                        )
                );

        when(
                historicalDataService
                        .downloadHistoricalData(
                                "TCS",
                                "compact"
                        )
        ).thenReturn(response);

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.loadSeries("TCS")
        );
    }
}