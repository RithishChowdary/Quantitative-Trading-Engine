package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.TradingRecord;

import com.rithish.trading.loader.CsvMarketDataLoader;
import com.rithish.trading.report.TradeReportService;

class TradeReportServiceTest {

    private TradeReportService tradeReportService;

    @BeforeEach
    void setUp() {
        tradeReportService =
                new TradeReportService();
    }

    @Test
    void shouldRejectNullBarSeries() {

        TradingRecord tradingRecord =
                createEmptyTradingRecord();

        assertThrows(
                NullPointerException.class,
                () -> tradeReportService.print(
                        null,
                        tradingRecord
                )
        );
    }

    @Test
    void shouldRejectNullTradingRecord() {

        BarSeries series =
                new CsvMarketDataLoader()
                        .loadSeries("TCS");

        assertThrows(
                NullPointerException.class,
                () -> tradeReportService.print(
                        series,
                        null
                )
        );
    }

    @Test
    void shouldPrintReportForValidEmptyTradingRecord() {

        BarSeries series =
                new CsvMarketDataLoader()
                        .loadSeries("TCS");

        TradingRecord tradingRecord =
                createEmptyTradingRecord();

        assertDoesNotThrow(
                () -> tradeReportService.print(
                        series,
                        tradingRecord
                )
        );
    }

    @Test
    void shouldPrintReportForValidTradingRecord() {

        BarSeries series =
                new CsvMarketDataLoader()
                        .loadSeries("TCS");

       TradingRecord tradingRecord =
        createTradingRecord(series);

        assertDoesNotThrow(
                () -> tradeReportService.print(
                        series,
                        tradingRecord
                )
        );
    }

    private TradingRecord createEmptyTradingRecord() {

        return new org.ta4j.core.BaseTradingRecord();
    }

   private TradingRecord createTradingRecord(
        BarSeries series) {

    TradingRecord tradingRecord =
            new BaseTradingRecord();

    tradingRecord.enter(
            10,
            series.numFactory().numOf(100.0),
            series.numFactory().one()
    );

    tradingRecord.exit(
            20,
            series.numFactory().numOf(110.0),
            series.numFactory().one()
    );

    return tradingRecord;
}
}