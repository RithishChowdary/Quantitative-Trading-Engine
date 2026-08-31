package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.TradingRecord;

import com.rithish.trading.loader.CsvMarketDataLoader;
import com.rithish.trading.report.PerformanceMetrics;
import com.rithish.trading.report.PerformanceReportService;

class PerformanceReportServiceTest {

    private PerformanceReportService reportService;

    @BeforeEach
    void setUp() {
        reportService =
                new PerformanceReportService();
    }

    @Test
    void shouldRejectNullBarSeries() {

        TradingRecord tradingRecord =
                new BaseTradingRecord();

        PerformanceMetrics metrics =
                createMetrics();

        assertThrows(
                NullPointerException.class,
                () -> reportService.print(
                        null,
                        tradingRecord,
                        metrics
                )
        );
    }

    @Test
    void shouldRejectNullTradingRecord() {

        BarSeries series =
                new CsvMarketDataLoader()
                        .loadSeries("TCS");

        PerformanceMetrics metrics =
                createMetrics();

        assertThrows(
                NullPointerException.class,
                () -> reportService.print(
                        series,
                        null,
                        metrics
                )
        );
    }

    @Test
    void shouldRejectNullMetrics() {

        BarSeries series =
                new CsvMarketDataLoader()
                        .loadSeries("TCS");

        TradingRecord tradingRecord =
                new BaseTradingRecord();

        assertThrows(
                NullPointerException.class,
                () -> reportService.print(
                        series,
                        tradingRecord,
                        null
                )
        );
    }

    @Test
    void shouldPrintReportForValidData() {

        BarSeries series =
                new CsvMarketDataLoader()
                        .loadSeries("TCS");

        TradingRecord tradingRecord =
                new BaseTradingRecord();

        PerformanceMetrics metrics =
                createMetrics();

        assertDoesNotThrow(
                () -> reportService.print(
                        series,
                        tradingRecord,
                        metrics
                )
        );
    }

    private PerformanceMetrics createMetrics() {

        return new PerformanceMetrics(
                10,       // totalTrades
                6,        // winningTrades
                4,        // losingTrades
                0.60,     // winRate
                1000.0,   // totalProfit
                500.0,    // totalLoss
                500.0,    // totalEarnings
                50.0,     // averageProfit
                200.0,    // maximumDrawdown
                2.0       // profitFactor
        );
    }
}